//********************************************************************************//
//																				  //
//		- "Talk to me like I'm a 3 year old!" Programming Lessons -				  //
//																				  //
//		$Author:		DigiBen		digiben@gametutorials.com					  //
//		$Author of port:Abdul Bezrati abezrati@hotmail.com	     				  //
//																				  //
//		$Program:		MD3 Animation											  //
//																				  //
//		$Description:	Demonstrates animating Quake3 characters with quaternions //
//																				  //
//		$Date:			3/28/02													  //
//																				  //
//********************************************************************************//


///////////// *** NEW *** ////////// *** NEW *** ///////////// *** NEW *** ////////////////////

///////////////////////////////////////////////////////////////////////////////////
//
// This version of the tutorial incorporates the animation data stored in the MD3
// character files.  We will be reading in the .cfg file that stores the animation
// data.  The rotations and translations of the models will be done using a matrix.
// There will be no more calls to glTranslatef().  To create the rotation and
// translation matrix, quaternions will be used.  This is because quaternions
// are excellent for interpolating between 2 rotations, as well as not overriding
// another translation causing "gimbal lock".
//
// So, why do we need to interpolate?  Well, the animations for the character are
// stored in key frames.  Instead of saving each frame of an animation, key frames
// are stored to cut down on memory and disk space.  The files would be huge if every
// frame was saved for every animation, as well as creating a huge memory footprint.
// Can you imagine having 10+ models in memory with all of that animation data?
//
// The animation key frames are stored in 2 ways.  The torso and legs mesh have vertices
// stored for each of the key frames, along with separate rotations and translations
// for the basic bone animation.  Remember, that each .md3 represents a bone, that needs
// to be connected at a joint.  For instance, the torso is connected to the legs, and the
// head is connected to the torso.  So, that makes 3 bones and 2 joints.  If you add the
// weapon, the weapon is connected to the hand joint, which gives us 4 bones and 3 joints.
// Unlike conventional skeletal animation systems, the main animations of the character's
// movement, such as a gesture or swimming animation, are done not with bones, but with
// vertex key frames, like in the .md2 format. Since the lower, upper, head and weapon models
// are totally different models, which aren't seamlessly connected to each other, then parent
// node needs to end a message (a translation and rotation) down to all it's child nodes to
// tell them where they need to be in order for the animation to look right.  A good example
// of this is when the legs has the DEATH3 animation set,  The legs might kick back into a back
// flip that lands the character on their face, dead.  Well, since the main models are separate,
// if the legs didn't tell the torso where to go, then the model's torso would stay in the same
// place and the body would detach itself from the legs.  The exporter calculates all this stuff
// for you of course.
//
// But getting back to the interpolation, since we use key frames, if we didn't interpolate
// between them, the animation would look very jumping and unnatural.  It would also go too
// fast.  By interpolating, we create a smooth transition between each key frame.
//
// As seen in the .md2 tutorials, interpolating between vertices is easy if we use the
// linear interpolation function:  p(t) = p0 + t(p1 - p0).  The same goes for translations,
// since it's just 2 3D points.  This is not so for the rotations.  The Quake3 character
// stores the rotations for each key frame in a 3x3 matrix.  This isn't a simple linear
// interpolation that needs to be performed.  If we convert the matrices to a quaternion,
// then use spherical linear interpolation (SLERP) between the current frame's quaternion
// and the next key frame's quaternion, we will have a new interpolated quaternion that
// can be converted into a 4x4 matrix to be applied to the current model view matrix in OpenGL.
// After finding the interpolated translation to be applied, we can slip that into the rotation
// matrix before it's applied to the current matrix, which will require only one matrix command.
//
// You'll notice that in the CreateFromMatrix() function in our quaternion class, I allow a
// row and column count to be passed in.  This is just a dirty way to allow a 3x3 or 4x4 matrix
// to be passed in.  Instead of creating a whole new function and copy and pasting the main
// code, it seemed fitting for a tutorial.  It's obvious that the quaternion class is missing
// a tremendous amount of functions, but I chose to only keep the functions that we would use.
//
// For those of you who don't know what interpolation us, here is a section abstracted
// from the MD2 Animation tutorial:
//
// -------------------------------------------------------------------------------------
// Interpolation: Gamedev.net's Game Dictionary say interpolation is "using a ratio
// to step gradually a variable from one value to another."  In our case, this
// means that we gradually move our vertices from one key frame to another key frame.
// There are many types of interpolation, but we are just going to use linear.
// The equation for linear interpolation is this:
//
//				p(t) = p0 + t(p1 - p0)
//
//				t - The current time with 0 being the start and 1 being the end
//				p(t) - The result of the equation with time t
//				p0 - The starting position
//				p1 - The ending position
//
// Let's throw in an example with numbers to test this equation.  If we have
// a vertex stored at 0 along the X axis and we wanted to move the point to
// 10 with 5 steps, see if you can fill in the equation without a time just yet.
//
// Finished?  You should have come up with:
//
//				p(t) = 0 + t(10 - 0)
//				p(t) = 0 + 10t
//				p(t) = 10t
//
// Now, all we need it a time from 0 to 1 to pass in, which will allow us to find any
// point from 0 to 10, depending on the time.  Since we wanted to find out the distance
// we need to travel each frame if we want to reach the end point in 5 steps, we just
// divide 1 by 5: 1/5 = 0.2
//
// We can then pass this into our equation:
//
//				p(0.2) = 10 * 0.2
//				p(0.2) = 2
//
// What does that tell us?  It tells us we need to move the vertex along the x
// axis each frame by a distance of 2 to reach 10 in 5 steps.  Yah yah, this isn't
// rocket science, but it's important to know that what your mind would have done
// immediately without thinking about it, is linear interpolation.
//
// Are you starting to see how this applies to our model?  If we only read in key
// frames, then we need to interpolate every vertex between the current and next
// key frame for animation.  To get a perfect idea of what is going on, try
// taking out the interpolation and just render the key frames.  You will notice
// that you can still see what is kinda going on, but it moves at an incredible pace!
// There is not smoothness, just a really fast jumpy animation.
// ------------------------------------------------------------------------------------
//
// Let's jump into the code (hold your breath!)
//
//

package org.codejive.world3d.loaders.md3;

import java.util.StringTokenizer;
import java.util.Vector;
import java.io.*;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.swing.JOptionPane;

public class MD3Loader
{

  GL  gl;
  GLU glu;

  // If one model is set to one of the BOTH_* animations, the other one should be too,
  // otherwise it looks really bad and confusing.
  int BOTH_DEATH1    =  0,   // The first twirling death animation
	  BOTH_DEAD1     =  1,   // The end of the first twirling death animation
	  BOTH_DEATH2    =  2,   // The second twirling death animation
	  BOTH_DEAD2     =  3,   // The end of the second twirling death animation
	  BOTH_DEATH3    =  4,   // The back flip death animation
	  BOTH_DEAD3     =  5,   // The end of the back flip death animation

							 // The next block is the animations that the upper body performs

	  TORSO_GESTURE  =  6,   // The torso's gesturing animation
	  TORSO_ATTACK   =  7,   // The torso's attack1 animation
	  TORSO_ATTACK2  =  8,   // The torso's attack2 animation
	  TORSO_DROP     =  9,   // The torso's weapon drop animation
	  TORSO_RAISE    = 10,   // The torso's weapon pickup animation
	  TORSO_STAND    = 11,   // The torso's idle stand animation
	  TORSO_STAND2   = 12,   // The torso's idle stand2 animation
							 // The final block is the animations that the legs perform

	  LEGS_WALKCR    = 13,   // The legs's crouching walk animation
	  LEGS_WALK      = 14,   // The legs's walk animation
	  LEGS_RUN       = 15,   // The legs's run animation
	  LEGS_BACK      = 16,   // The legs's running backwards animation
	  LEGS_SWIM      = 17,   // The legs's swimming animation
	  LEGS_JUMP      = 18,   // The legs's jumping animation
	  LEGS_LAND      = 19,   // The legs's landing animation
	  LEGS_JUMPB     = 20,   // The legs's jumping back animation
	  LEGS_LANDB     = 21,   // The legs's landing back animation
	  LEGS_IDLE      = 22,   // The legs's idle stand animation
	  LEGS_IDLECR    = 23,   // The legs's idle crouching animation
	  LEGS_TURN      = 24,   // The legs's turn animation
	  MAX_ANIMATIONS = 25;   // The define for the maximum amount of animations

  // This holds the header information that is read in at the beginning of the file
  class tMd3Header{
					String strFile,    // This stores the name of the file
						   fileID;     // This stores the file ID - Must be "IDP3"
					int    numMaxSkins,// This stores the number of skins for the mesh
						   headerSize, // This stores the mesh header size
						   numMeshes,  // This stores the number of sub-objects in the mesh
						   numFrames,  // This stores the number of animation frames
						   fileSize,   // This stores the file size
						   tagStart,   // This stores the offset into the file for tags
						   numTags,    // This stores the tag count
						   version,    // This stores the file version - Must be 15
						   tagEnd;     // This stores the end offset into the file for tags
				   tMd3Header(){
					 fileID         = bytesToString(4);
					 m_FilePointer +=            4;
					 version        = bytesToInt();
					 strFile        = bytesToString(68);
					 m_FilePointer +=           68;
					 numFrames      = bytesToInt();
					 numTags        = bytesToInt();
					 numMeshes      = bytesToInt();
					 numMaxSkins    = bytesToInt();
					 headerSize     = bytesToInt();
					 tagStart       = bytesToInt();
					 tagEnd         = bytesToInt();
					 fileSize       = bytesToInt();
					 }
				   }

  // This structure is used to read in the mesh data for the .md3 models
  class tMd3MeshInfo{
					  String strName,      // This stores the mesh name (We do care)
							 meshID;       // This stores the mesh ID (We don't care)
					  int    numMeshFrames,// This stores the mesh aniamtion frame count
							 numTriangles, // This stores the mesh face count
							 numVertices,  // This stores the mesh vertex count
							 vertexStart,  // This stores the starting offset for the vertex indices
							 headerSize,   // This stores the header size for the mesh
							 numSkins,     // This stores the mesh skin count
							 meshSize,     // This stores the total mesh size
							 triStart,     // This stores the starting offset for the triangles
							 uvStart;      // This stores the starting offset for the UV coordinates
					  tMd3MeshInfo(){
						meshID         = bytesToString(4);
						m_FilePointer += 4;
						strName        = bytesToString(68);
						m_FilePointer += 68;
						numMeshFrames  = bytesToInt();
						numSkins       = bytesToInt();
						numVertices    = bytesToInt();
						numTriangles   = bytesToInt();
						triStart       = bytesToInt();
						headerSize     = bytesToInt();
						uvStart        = bytesToInt();
						vertexStart    = bytesToInt();
						meshSize       = bytesToInt();
					  }
					}

  // This is our tag structure for the .MD3 file format.  These are used link other
  // models to and the rotate and transate the child models of that model.
  class tMd3Tag{
				 CVector3 vPosition    = new CVector3();  // This stores the translation that should be performed
				 String   strName;                        // This stores the name of the tag (I.E. "tag_torso")
				 float    rotation[] = new float[9]; // This stores the 3x3 rotation matrix for this frame
				 tMd3Tag(){
				   strName        = bytesToString(64);
				   m_FilePointer += 64;
				   vPosition.x    = bytesToFloat();
				   vPosition.y    = bytesToFloat();
				   vPosition.z    = bytesToFloat();
				   for (int i=0;i <9 ;i++ )
					 rotation[i] = bytesToFloat();
				 }
			   }

  // This stores the bone information (useless as far as I can see...)
  class tMd3Bone{
				  String creator;                   // The modeler used to create the model (I.E. "3DS Max")
				  float  position[]= new float[3], // This supposedly stores the bone position???
						 mins[]    = new float[3], // This is the min (x, y, z) value for the bone
						 maxs[]    = new float[3], // This is the max (x, y, z) value for the bone
						 scale;                    // This stores the scale of the bone
				  tMd3Bone(){
					mins[0]        = bytesToFloat();
					mins[1]        = bytesToFloat();
					mins[2]        = bytesToFloat();
					maxs[0]        = bytesToFloat();
					maxs[1]        = bytesToFloat();
					maxs[2]        = bytesToFloat();
					position[0]    = bytesToFloat();
					position[1]    = bytesToFloat();
					position[2]    = bytesToFloat();
					scale          = bytesToFloat();
					creator        = bytesToString(16);
					m_FilePointer += 16;
				  }
				}

  // This stores the normals and vertex indices
  class tMd3Triangle{
					  float vertex[] = new float[3]; // The vertex for this face (scale down by 64.0f)
					  int   normal[] = new int[2];  // This stores some crazy normal values (not sure...)
					  tMd3Triangle(){
						vertex[0] = bytesToShort();
						vertex[1] = bytesToShort();
						vertex[2] = bytesToShort();
						normal[0] = unsignedByte();
						normal[1] = unsignedByte();
					  }
					}

  // This stores the indices into the vertex and texture coordinate arrays
  class tMd3Face{
				  int vertexIndices[] = new int[3];
				  tMd3Face(){
					vertexIndices[0] = bytesToInt();
					vertexIndices[1] = bytesToInt();
					vertexIndices[2] = bytesToInt();
				  }
				}

  // This stores UV coordinates
  class tMd3TexCoord{
					  float[] textureCoord = new float[2];
					  tMd3TexCoord(){
						textureCoord[0] = bytesToFloat();
						textureCoord[1] = bytesToFloat();
					  }
					}

  // This stores a skin name (We don't use this, just the name of the model to get the texture)
  class tMd3Skin{
				  String strName;
				  tMd3Skin(){
					strName        = bytesToString(68);
					m_FilePointer +=                68;
				  }
				}

  // This is our 3D point class.  This will be used to store the vertices of our model.
  class CVector3{
				  float x, y, z;
				}

  // This is our 2D point class.  This will be used to store the UV coordinates.
  class CVector2{
				  float x, y;
				}

  // This is our face structure.  This is is used for indexing into the vertex
  // and texture coordinate arrays.  From this information we know which vertices
  // from our vertex array go to which face, along with the correct texture coordinates.
  class tFace{
			   int[] vertIndex  = new int[3], // indicies for the verts that make up this triangle
					 coordIndex = new int[3]; // indicies for the tex coords to texture this face
			 }

  // This holds the information for a material.  It may be a texture map of a color.
  // Some of these are not used, but I left them because you will want to eventually
  // read in the UV tile ratio and the UV tile offset for some models.
  // This holds the information for a material.  It may be a texture map of a color.
  // Some of these are not used, but I left them.
  class tMaterialInfo{
						String strName,               // The texture name
							   strFile;               // The texture file name (If this is set it's a texture map)
						float  uTile,                 // u tiling of texture
							   vTile,                 // v tiling of texture
							   uOffset,               // u offset of texture
							   vOffset;               // v offset of texture
						byte   color[] = new byte[3]; // The color of the object (R, G, B)
						int    texureId;              // the texture ID
					 }

  //////////// *** NEW *** ////////// *** NEW *** ///////////// *** NEW *** ////////////////////

  // This holds our information for each animation of the Quake model.
  // A STL vector list of this structure is created in our t3DModel structure below.
  class tAnimationInfo{
						String strName;         // This stores the name of the animation (I.E. "TORSO_STAND")
						int    endFrame,        // This stores the last frame number for this animation
							   startFrame,      // This stores the first frame number for this animation
							   loopingFrames,   // This stores the looping frames for this animation (not used)
							   framesPerSecond; // This stores the frames per second that this animation runs
					  }

//////////// *** NEW *** ////////// *** NEW *** ///////////// *** NEW *** ////////////////////
  // This holds all the information for our model/scene.
  // You should eventually turn into a robust class that
  // has loading/drawing/querying functions like:
  // LoadModel(...); DrawObject(...); DrawModel(...); DestroyModel(...);
  class t3DObject{
				   CVector3 pVerts[],    // The object's vertices
							pNormals[];  // The object's normals
				   CVector2 pTexVerts[]; // The texture's UV coordinates
				   boolean  bHasTexture; // This is TRUE if there is a texture map for this object
				   String   strName;     // The name of the object
				   tFace    pFaces[];    // The faces information of the object
				   int      numOfVerts,  // The number of verts in the model
							numOfFaces,  // The number of faces in the model
							numTexVertex,// The number of texture coordinates
							materialID;  // The texture ID to use, which is the index into our texture array
				 }

// This our model structure
  class t3DModel{

				  int       numOfTags,                  // This stores the number of tags in the model
							nextFrame,                  // The next frame of animation to interpolate too
							currentAnim,                // The current index into pAnimations list
							currentFrame,               // The current frame of the current animation
							numOfObjects,               // The number of objects in the model
							numOfMaterials,             // The number of materials for the model
							numOfAnimations;            // The number of animations in this model
	  float     t;                          // The ratio of 0.0f to 1.0f between each key frame
	  double    lastTime;                   // This stores the last time that was stored
	  Vector<tAnimationInfo>
	  			pAnimations = new Vector<tAnimationInfo>(); // The list of animations
	  Vector<tMaterialInfo>
	  			pMaterials  = new Vector<tMaterialInfo>(); // The list of material information (Textures and colors)
	  Vector<t3DObject>
				pObject     = new Vector<t3DObject>(); // The object list for our model
	  tMd3Tag   pTags[];                    // This stores all the tags for the model animations
	  t3DModel  pLinks[];                   // This stores a list of pointers that are linked to this model
  }

  int unsignedByte(){
	int i = (fileContents[m_FilePointer++] & 0xFF);
	return i;
  }

  short bytesToShort(){
	int s1 = (fileContents[m_FilePointer++] & 0xFF),
		s2 = (fileContents[m_FilePointer++] & 0xFF) << 8;
	return ((short)(s1 | s2));
  }

  int bytesToInt(){
	return   (fileContents[m_FilePointer++] & 0xFF)      |
			 (fileContents[m_FilePointer++] & 0xFF) <<  8|
			 (fileContents[m_FilePointer++] & 0xFF) << 16|
			 (fileContents[m_FilePointer++] & 0xFF) << 24;
  }

  float bytesToFloat(){
	return Float.intBitsToFloat(bytesToInt());
  }

  String bytesToString(int size){
	//Look for zero terminated string from byte array
	for(int i=m_FilePointer;i<m_FilePointer + size ;i++ )
	  if((fileContents[i] & 0xFF)== (byte)0)
		return new String(fileContents, m_FilePointer, i - m_FilePointer);
	return new String(fileContents,m_FilePointer, size);
  }

  tMd3TexCoord  m_pTexCoords[]; // The texture coordinates
  tMd3Triangle  m_pVertices[];  // Vertex/UV indices
  tMd3Header    m_Header;       // The header data
  tMd3Face      m_pTriangles[]; // Face/Triangle data
  tMd3Skin      m_pSkins[];     // The skin name data (not used)
  tMd3Bone      m_pBones[];     // This stores the bone data (not used)
  t3DModel      m_Weapon,       // This stores the players weapon model (optional load)
				m_Upper,        // This stores the players upper body part
				m_Lower,        // This stores the players lower body part
				m_Head;         // This stores the players head body part
  Vector<String> strTextures;
  byte[]        fileContents;
  int           kUpper  = 1,    // This stores the ID for the torso model
				kHead   = 2,    // This stores the ID for the head model
				kLower  = 0,    // This stores the ID for the legs model
				kWeapon = 3,    // This stores the ID for the weapon model
				MAX_TEXTURES = 100,
				m_Textures[],
				m_FilePointer;

  ///////////////////////////////// CMODEL MD3 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
  /////
  /////	This is our CModelMD3 constructor
  /////
  ///////////////////////////////// CMODEL MD3 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

  public MD3Loader(GL gll, GLU gluu){
	gl          = gll;
	glu         = gluu;
	m_Head      = new t3DModel();
	m_Upper     = new t3DModel();
	m_Lower     = new t3DModel();
	m_Weapon    = new t3DModel();
	m_Textures  = new int[MAX_TEXTURES];
	strTextures = new Vector<String>();
  }

  ///////////////////////////////// IS IN STRING \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
  /////
  /////	This returns true if the string strSubString is inside of strString
  /////
  ///////////////////////////////// IS IN STRING \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

  boolean IsInString(String strString, String strSubString){

	// Make sure both of these strings are valid, return false if any are empty
	if(strString.length() <= 0 || strSubString.length() <= 0) return false;

	// Make sure the index returned was valid
	if(strString.indexOf(strSubString) != -1)
	  return true;
	// The sub string does not exist in strString.
	return false;
  }

  //////////// *** NEW *** ////////// *** NEW *** ///////////// *** NEW *** ////////////////////

  ///////////////////////////////// GET BODY PART \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
  /////
  /////	This returns a specific model from the character (kLower, kUpper, kHead, kWeapon)
  /////
  ///////////////////////////////// GET BODY PART \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

  t3DModel GetModel(int whichPart){
	// Return the legs model if desired
	if(whichPart == kLower)
	  return m_Lower;

	// Return the torso model if desired
	if(whichPart == kUpper)
	  return m_Upper;

	// Return the head model if desired
	if(whichPart == kHead)
	  return m_Head;

	// Return the weapon model
	return m_Weapon;
  }

  ///////////////////////////////// LOAD MODEL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
  /////
  /////	This loads our Quake3 model from the given path and character name
  /////
  ///////////////////////////////// LOAD MODEL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

  public boolean LoadModel(String strPath, String strModel){
	String strLowerModel, // This stores the file name for the lower.md3 model
		   strUpperModel, // This stores the file name for the upper.md3 model
		   strHeadModel,  // This stores the file name for the head.md3 model
		   strLowerSkin,  // This stores the file name for the lower.md3 skin
		   strUpperSkin,  // This stores the file name for the upper.md3 skin
		   strHeadSkin;   // This stores the file name for the head.md3 skin

	// This function is where all the character loading is taken care of.  We use
	// our CLoadMD3 class to load the 3 mesh and skins for the character. Since we
	// just have 1 name for the model, we add that to _lower.md3, _upper.md3 and _head.md3
	// to load the correct mesh files.

	// Make sure valid path and model names were passed in
	if( strPath == null ||  strModel == null)
	  return false;

	// Store the correct files names for the .md3 and .skin file for each body part.

	strLowerModel = strPath + "\\" + strModel + "_lower.MD3";
	strUpperModel = strPath + "\\" + strModel + "_upper.MD3";
	strHeadModel  = strPath + "\\" + strModel + "_head.MD3";

	// Get the skin file names with their path
	strLowerSkin = strPath + "\\" + strModel + "_lower.skin" ;
	strUpperSkin = strPath + "\\" + strModel + "_upper.skin";
	strHeadSkin  = strPath + "\\" + strModel + "_head.skin";

	// Next we want to load the character meshes.  The CModelMD3 class has member
	// variables for the head, upper and lower body parts.  These are of type t3DModel.
	// Depending on which model we are loading, we pass in those structures to ImportMD3.
	// This returns a true of false to let us know that the file was loaded okay.  The
	// appropriate file name to load is passed in for the last parameter.

	// Load the head mesh (*_head.md3) and make sure it loaded properly
	if(!ImportMD3(m_Head,  strHeadModel)){
	  // Display an error message telling us the file could not be found
	  JOptionPane.showMessageDialog(null,"Unable to load the HEAD model!",
										 "Error",
										 JOptionPane.ERROR_MESSAGE);
	  return false;
	}

	// Load the upper mesh (*_head.md3) and make sure it loaded properly
	if(!ImportMD3(m_Upper, strUpperModel)){
	  // Display an error message telling us the file could not be found
	  JOptionPane.showMessageDialog(null,"Unable to load the UPPER model!",
										 "Error",
										 JOptionPane.ERROR_MESSAGE);
	  return false;
	}

	// Load the lower mesh (*_lower.md3) and make sure it loaded properly
	if(!ImportMD3(m_Lower, strLowerModel)){
	  // Display an error message telling us the file could not be found
	  JOptionPane.showMessageDialog(null,"Unable to load the LOWER model!",
										 "Error",
										 JOptionPane.ERROR_MESSAGE);
	  return false;
	}

	// Load the lower skin (*_upper.skin) and make sure it loaded properly
	if(!LoadSkin(m_Lower, strLowerSkin)){
	  // Display an error message telling us the file could not be found
	  JOptionPane.showMessageDialog(null,"Unable to load the LOWER Skin!",
										 "Error",
										 JOptionPane.ERROR_MESSAGE);
	  return false;
	}

	// Load the upper skin (*_upper.skin) and make sure it loaded properly
	if(!LoadSkin(m_Upper, strUpperSkin)){
	  // Display an error message telling us the file could not be found
	  JOptionPane.showMessageDialog(null,"Unable to load the UPPER Skin!",
										 "Error",
										 JOptionPane.ERROR_MESSAGE);
	  return false;
	}

	// Load the head skin (*_head.skin) and make sure it loaded properly
	if(!LoadSkin(m_Head,  strHeadSkin)){
	  // Display an error message telling us the file could not be found
	  JOptionPane.showMessageDialog(null,"Unable to load the HEAD Skin!",
										 "Error",
										 JOptionPane.ERROR_MESSAGE);
	  return false;
	}

	// Once the models and skins were loaded, we need to load then textures.
	// We don't do error checking for this because we call CreateTexture() and
	// it already does error checking.  Most of the time there is only
	// one or two textures that need to be loaded for each character.  There are
	// different skins though for each character.  For instance, you could have a
	// army looking Lara Croft, or the normal look.  You can have multiple types of
	// looks for each model.  Usually it is just color changes though.

	// Load the lower, upper and head textures.
	LoadModelTextures(m_Lower, strPath);
	LoadModelTextures(m_Upper, strPath);
	LoadModelTextures(m_Head,  strPath);

	//////////// *** NEW *** ////////// *** NEW *** ///////////// *** NEW *** ////////////////////

	// We added to this function the code that loads the animation config file

	// This stores the file name for the .cfg animation file
	String strConfigFile;

	// Add the path and file name prefix to the animation.cfg file
	strConfigFile =  strPath + "\\" + strModel + "_animation.cfg";
	// Load the animation config file (*_animation.config) and make sure it loaded properly
	if(!LoadAnimations(strConfigFile)){
	  // Display an error message telling us the file could not be found
	  JOptionPane.showMessageDialog(null,"Unable to load the Animation Config File!",
										 "Error",
										 JOptionPane.ERROR_MESSAGE);
	  return false;
	}

	//////////// *** NEW *** ////////// *** NEW *** ///////////// *** NEW *** ////////////////////

	// Link the lower body to the upper body when the tag "tag_torso" is found in our tag array
	LinkModel(m_Lower, m_Upper, "tag_torso");

	// Link the upper body to the head when the tag "tag_head" is found in our tag array
	LinkModel(m_Upper, m_Head, "tag_head");

	// The character was loaded correctly so return true
	return true;
  }

  ///////////////////////////////// LOAD WEAPON \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
  /////
  /////	This loads a Quake3 weapon model from the given path and weapon name
  /////
  ///////////////////////////////// LOAD WEAPON \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

  public boolean LoadWeapon(String strPath, String strModel){

	String strWeaponModel,  // This stores the file name for the weapon model
		   strWeaponShader; // This stores the file name for the weapon shader.

	// Make sure the path and model were valid, otherwise return false
	if( strPath == null || strModel == null)
	  return false;

	// Concatenate the path and model name together
	strWeaponModel = strPath + "\\" + strModel + ".MD3";

	// Load the weapon mesh (*.md3) and make sure it loaded properly
	if(!ImportMD3(m_Weapon,  strWeaponModel)){
	  // Display an error message telling us the file could not be found
	  JOptionPane.showMessageDialog(null,"Unable to load the WEAPON Model!",
										 "Error",
										 JOptionPane.ERROR_MESSAGE);
	  return false;
	}

	// Add the path, file name and .shader extension together to get the file name and path
	strWeaponShader = strPath + "\\" + strModel + ".shader";

	// Load our textures associated with the gun from the weapon shader file
	if(!LoadShader(m_Weapon, strWeaponShader)){
	  // Display an error message telling us the file could not be found
	  JOptionPane.showMessageDialog(null,"Unable to load the SHADER file!",
										 "Error",
										 JOptionPane.ERROR_MESSAGE);
	  return false;
	}

	// We should have the textures needed for each weapon part loaded from the weapon's
	// shader, so let's load them in the given path.
	LoadModelTextures(m_Weapon, strPath);

	// Link the weapon to the model's hand that has the weapon tag
	LinkModel(m_Upper, m_Weapon, "tag_weapon");

	// The weapon loaded okay, so let's return true to reflect this
	return true;
  }

  ///////////////////////////////// LOAD MODEL TEXTURES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
  /////
  /////	This loads the textures for the current model passed in with a directory
  /////
  ///////////////////////////////// LOAD WEAPON \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

  void LoadModelTextures(t3DModel pModel, String strPath){

	// Go through all the materials that are assigned to this model
	for(int i = 0; i < pModel.numOfMaterials; i++){
	  // Check to see if there is a file name to load in this material
	  if((pModel.pMaterials.get(i)).strFile != null){
		// Create a boolean to tell us if we have a new texture to load
		boolean bNewTexture = true;
		// Go through all the textures in our string list to see if it's already loaded
		for(int j = 0; j < strTextures.size(); j++){
		// If the texture name is already in our list of texture, don't load it again.
		  if((pModel.pMaterials.get(i)).strFile.equals( (strTextures.get(j)))){
			// We don't need to load this texture since it's already loaded
			bNewTexture = false;
			// Assign the texture index to our current material textureID.
			// This ID will them be used as an index into m_Textures[].
			(pModel.pMaterials.get(i)).texureId = j;
		  }
		}
		// Make sure before going any further that this is a new texture to be loaded
		if(bNewTexture == false)
		  continue;
		String strFullPath;

		// Add the file name and path together so we can load the texture
		strFullPath = strPath + "/" + (pModel.pMaterials.get(i)).strFile;

		// We pass in a reference to an index into our texture array member variable.
		// The size() function returns the current loaded texture count.  Initially
		// it will be 0 because we haven't added any texture names to our strTextures list.
		CreateTexture(m_Textures, strFullPath, strTextures.size());
		// Set the texture ID for this material by getting the current loaded texture count
		(pModel.pMaterials.get(i)).texureId = strTextures.size();
		// Now we increase the loaded texture count by adding the texture name to our
		// list of texture names.  Remember, this is used so we can check if a texture
		// is already loaded before we load 2 of the same textures.  Make sure you
		// understand what an STL vector list is.  We have a tutorial on it if you don't.
		strTextures.add((pModel.pMaterials.get(i)).strFile);
	  }
	}
  }

  //////////// *** NEW *** ////////// *** NEW *** ///////////// *** NEW *** ////////////////////

  ///////////////////////////////// LOAD ANIMATIONS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
  /////
  /////	This loads the .cfg file that stores all the animation information
  /////
  ///////////////////////////////// LOAD ANIMATIONS \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

  boolean isDigit(char check){
	return (check >='0' && check <= '9');
  }

  boolean LoadAnimations(String strConfigFile){

	// This function is given a path and name to an animation config file to load.
	// The implementation of this function is arbitrary, so if you have a better way
	// to parse the animation file, that is just as good.  Whatever works.
	// Basically, what is happening here, is that we are grabbing an animation line:
	//
	// "0	31	0	25		// BOTH_DEATH1"
	//
	// Then parsing it's values.  The first number is the starting frame, the next
	// is the frame count for that animation (endFrame would equal startFrame + frameCount),
	// the next is the looping frames (ignored), and finally the frames per second that
	// the animation should run at.  The end of this line is the name of the animation.
	// Once we get that data, we store the information in our tAnimationInfo object, then
	// after we finish parsing the file, the animations are assigned to each model.
	// Remember, that only the torso and the legs objects have animation.  It is important
	// to note also that the animation prefixed with BOTH_* are assigned to both the legs
	// and the torso animation list, hence the name "BOTH" :)

	// Create an animation object for every valid animation in the Quake3 Character
	tAnimationInfo animations[] = new tAnimationInfo[MAX_ANIMATIONS];

	try{
	  BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(strConfigFile)));

	  String strWord  = "",  // This stores the current word we are reading in
			 strLine  = "";  // This stores the current line we read in
	  int currentAnim = 0,   // This stores the current animation count
		  torsoOffset = 0;  // The offset between the first torso and leg animation

	  // Here we go through every word in the file until a numeric number if found.
	  // This is how we know that we are on the animation lines, and past header info.
	  // This of course isn't the most solid way, but it works fine.  It wouldn't hurt
	  // to put in some more checks to make sure no numbers are in the header info.
	  while((strWord = reader.readLine()) != null){

		//Empty String, ignore and go the next
		if(strWord.length() == 0)
		  continue;
		// If the first character of the word is NOT a number, we haven't hit an animation line
		if(!isDigit(strWord.charAt(0))){
		  strLine += strWord;
		  continue;
		}

		// If we get here, we must be on an animation line, so let's parse the data.
		// We should already have the starting frame stored in strWord, so let's extract it.

		//Parse my line
		StringTokenizer tokenizer = new StringTokenizer(strWord);
		// Get the number stored in the strWord string and create some variables for the rest
		int startFrame = Integer.parseInt(tokenizer.nextToken());
		int numOfFrames = 0, loopingFrames = 0, framesPerSecond = 0;

		// Read in the number of frames, the looping frames, then the frames per second
		// for this current animation we are on.
		numOfFrames     = Integer.parseInt(tokenizer.nextToken());
		loopingFrames   = Integer.parseInt(tokenizer.nextToken());
		framesPerSecond = Integer.parseInt(tokenizer.nextToken());

		// Initialize the current animation structure with the data just read in

		// Read past the "//" and read in the animation name (I.E. "BOTH_DEATH1").
		// This might not be how every config file is set up, so make sure.
		strLine = tokenizer.nextToken();
		if(strLine.equals("//"))
		  strLine = tokenizer.nextToken();

		// Initialize the current animation structure with the data just read in
		animations[currentAnim]                 = new tAnimationInfo();
		animations[currentAnim].startFrame      = startFrame;
		animations[currentAnim].endFrame        = startFrame + numOfFrames;
		animations[currentAnim].loopingFrames   = loopingFrames;
		animations[currentAnim].framesPerSecond = framesPerSecond;
		// Copy the name of the animation to our animation structure
		animations[currentAnim].strName         = strLine;

		// If the animation is for both the legs and the torso, add it to their animation list
		if(IsInString(strLine, "BOTH")){
		  // Add the animation to each of the upper and lower mesh lists
		  m_Upper.pAnimations.add(animations[currentAnim]);
		  m_Lower.pAnimations.add(animations[currentAnim]);
		}
		// If the animation is for the torso, add it to the torso's list
		else
		  if(IsInString(strLine, "TORSO")){
			m_Upper.pAnimations.add(animations[currentAnim]);
		  }
		  // If the animation is for the legs, add it to the legs's list
		  else
			if(IsInString(strLine, "LEGS")){
			  // Because I found that some config files have the starting frame for the
			  // torso and the legs a different number, we need to account for this by finding
			  // the starting frame of the first legs animation, then subtracting the starting
			  // frame of the first torso animation from it.  For some reason, some exporters
			  // might keep counting up, instead of going back down to the next frame after the
			  // end frame of the BOTH_DEAD3 anim.  This will make your program crash if so.

			  // If the torso offset hasn't been set, set it
			  if(torsoOffset == 0)
				torsoOffset = animations[LEGS_WALKCR].startFrame - animations[TORSO_GESTURE].startFrame;

			  // Minus the offset from the legs animation start and end frame.
			  animations[currentAnim].startFrame -= torsoOffset;
			  animations[currentAnim].endFrame   -= torsoOffset;

			  // Add the animation to the list of leg animations
			  m_Lower.pAnimations.add(animations[currentAnim]);
			}
			// Increase the current animation count
			currentAnim++;
	  }
	  // Store the number if animations for each list by the STL vector size() function
	  m_Weapon.numOfAnimations = m_Head.pAnimations.size();
	  m_Lower.numOfAnimations  = m_Lower.pAnimations.size();
	  m_Upper.numOfAnimations  = m_Upper.pAnimations.size();
	  m_Head.numOfAnimations   = m_Head.pAnimations.size();
	}
	catch(IOException io){
	  JOptionPane.showMessageDialog(null,"Unable to load the Animation file!",
										 "Error",
										 JOptionPane.ERROR_MESSAGE);
	  return false;
	}
	// Return a success
	return true;
  }

  ///////////////////////////////// LINK MODEL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
  /////
  /////	This links the body part models to each other, along with the weapon
  /////
  ///////////////////////////////// LINK MODEL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

  void LinkModel(t3DModel  pModel, t3DModel  pLink, String strTagName){
	// Make sure we have a valid model, link and tag name, otherwise quit this function
	if(pModel == null || pLink == null || strTagName == null ) return;

	// Go through all of our tags and find which tag contains the strTagName, then link'em
	for(int i = 0; i < pModel.numOfTags; i++){
	  // If this current tag index has the tag name we are looking for
	  if(pModel.pTags[i].strName.equals(strTagName)){
		// Link the model's link index to the link (or model/mesh) and return
		pModel.pLinks[i] = pLink;
		return;
	  }
	}
  }

  //////////// *** NEW *** ////////// *** NEW *** ///////////// *** NEW *** ////////////////////

  ///////////////////////////////// UPDATE MODEL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
  /////
  /////	This sets the current frame of animation, depending on it's fps and t
  /////
  ///////////////////////////////// UPDATE MODEL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

  void UpdateModel(t3DModel  pModel){

	// Initialize a start and end frame, for models with no animation
	int startFrame = 0,
		endFrame   = 1;

	// This function is used to keep track of the current and next frames of animation
	// for each model, depending on the current animation.  Some models down have animations,
	// so there won't be any change.

	// Here we grab the current animation that we are on from our model's animation list
	tAnimationInfo  pAnim = pModel.pAnimations.get(pModel.currentAnim);

	// If there is any animations for this model
	if(pModel.numOfAnimations != 0){
	  // Set the starting and end frame from for the current animation
	  startFrame = pAnim.startFrame;
	  endFrame   = pAnim.endFrame;
	}

	// This gives us the next frame we are going to.  We mod the current frame plus
	// 1 by the current animations end frame to make sure the next frame is valid.
	pModel.nextFrame = (pModel.currentFrame + 1) % endFrame;

	// If the next frame is zero, that means that we need to start the animation over.
	// To do this, we set nextFrame to the starting frame of this animation.
	if(pModel.nextFrame == 0)
	  pModel.nextFrame =  startFrame;

	// Next, we want to get the current time that we are interpolating by.  Remember,
	// if t = 0 then we are at the beginning of the animation, where if t = 1 we are at the end.
	// Anything from 0 to 1 can be thought of as a percentage from 0 to 100 percent complete.
	SetCurrentTime(pModel);
  }

  //////////// *** NEW *** ////////// *** NEW *** ///////////// *** NEW *** ////////////////////


  ///////////////////////////////// DRAW MODEL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
  /////
  /////	This recursively draws all the character nodes, starting with the legs
  /////
  ///////////////////////////////// DRAW MODEL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

  public void DrawModel(){
	// Rotate the model to compensate for the z up orientation that the model was saved
	gl.glRotatef(-90, 1, 0, 0);

	//////////// *** NEW *** ////////// *** NEW *** ///////////// *** NEW *** ////////////////////

	  // Since we have animation now, when we draw the model the animation frames need
	  // to be updated.  To do that, we pass in our lower and upper models to UpdateModel().
	  // There is no need to pass in the head of weapon, since they don't have any animation.

	// Update the leg and torso animations
	UpdateModel(m_Lower);
	UpdateModel(m_Upper);

	//////////// *** NEW *** ////////// *** NEW *** ///////////// *** NEW *** ////////////////////

	// Draw the first link, which is the lower body.  This will then recursively go
	// through the models attached to this model and drawn them.
	DrawLink(m_Lower);
  }

  ///////////////////////////////// DRAW LINK \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
  /////
  /////	This draws the current mesh with an effected matrix stack from the last mesh
  /////
  ///////////////////////////////// DRAW LINK \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

  void DrawLink(t3DModel pModel){

	// Draw the current model passed in (Initially the legs)
	RenderModel(pModel);

	//////////// *** NEW *** ////////// *** NEW *** ///////////// *** NEW *** ////////////////////
	// Though the changes to this function from the previous tutorial aren't huge, they
	// are pretty powerful.  Since animation is in effect, we need to create a rotational
	// matrix for each key frame, at each joint, to be applied to the child nodes of that
	// object.  We can also slip in the interpolated translation into that same matrix.
	// The big thing in this function is interpolating between the 2 rotations.  The process
	// involves creating 2 quaternions from the current and next key frame, then using
	// slerp (spherical linear interpolation) to find the interpolated quaternion, then
	// converting that quaternion to a 4x4 matrix, adding the interpolated translation
	// to that matrix, then finally applying it to the current model view matrix in OpenGL.
	// This will then effect the next objects that are somehow explicitly or inexplicitly
	// connected and drawn from that joint.
	// Create some local variables to store all this crazy interpolation data

	CQuaternion qQuat             = new CQuaternion(),
				qNextQuat         = new CQuaternion(),
				qInterpolatedQuat = new CQuaternion();
	float[] pMatrix,
			pNextMatrix,
			finalMatrix = new float[16];

	//////////// *** NEW *** ////////// *** NEW *** ///////////// *** NEW *** ////////////////////
	// Now we need to go through all of this models tags and draw them.
	for(int i = 0; i < pModel.numOfTags; i++){
	  // Get the current link from the models array of links (Pointers to models)
	  t3DModel pLink = pModel.pLinks[i];
	  // If this link has a valid address, let's draw it!
	  if(pLink != null){
		//////////// *** NEW *** ////////// *** NEW *** ///////////// *** NEW *** ////////////////////
		// To find the current translation position for this frame of animation, we times
		// the currentFrame by the number of tags, then add i.  This is similar to how
		// the vertex key frames are interpolated.
		CVector3 vOldPosition = pModel.pTags[pModel.currentFrame * pModel.numOfTags + i].vPosition;

		// Grab the next key frame translation position
		CVector3 vNextPosition = pModel.pTags[pModel.nextFrame * pModel.numOfTags + i].vPosition;
		// By using the equation: p(t) = p0 + t(p1 - p0), with a time t,
		// we create a new translation position that is closer to the next key frame.
		CVector3 vPosition = new CVector3();
		vPosition.x = vOldPosition.x + pModel.t * (vNextPosition.x - vOldPosition.x);
		vPosition.y	= vOldPosition.y + pModel.t * (vNextPosition.y - vOldPosition.y);
		vPosition.z	= vOldPosition.z + pModel.t * (vNextPosition.z - vOldPosition.z);

		// Now comes the more complex interpolation.  Just like the translation, we
		// want to store the current and next key frame rotation matrix, then interpolate
		// between the 2.

		// Get a pointer to the start of the 3x3 rotation matrix for the current frame
		pMatrix = pModel.pTags[pModel.currentFrame * pModel.numOfTags + i].rotation;

		// Get a pointer to the start of the 3x3 rotation matrix for the next frame
		pNextMatrix = pModel.pTags[pModel.nextFrame * pModel.numOfTags + i].rotation;

		// Now that we have 2 1D arrays that store the matrices, let's interpolate them

		// Convert the current and next key frame 3x3 matrix into a quaternion
		qQuat.CreateFromMatrix( pMatrix, 3);
		qNextQuat.CreateFromMatrix( pNextMatrix, 3 );

		// Using spherical linear interpolation, we find the interpolated quaternion
		qInterpolatedQuat = qQuat.Slerp(qQuat, qNextQuat, pModel.t);

		// Here we convert the interpolated quaternion into a 4x4 matrix
		qInterpolatedQuat.CreateMatrix( finalMatrix );

		// To cut out the need for 2 matrix calls, we can just slip the translation
		// into the same matrix that holds the rotation.  That is what index 12-14 holds.
		finalMatrix[12] = vPosition.x;
		finalMatrix[13] = vPosition.y;
		finalMatrix[14] = vPosition.z;

		//////////// *** NEW *** ////////// *** NEW *** ///////////// *** NEW *** ////////////////////
		// Start a new matrix scope
		gl.glPushMatrix();

		//////////// *** NEW *** ////////// *** NEW *** ///////////// *** NEW *** ////////////////////
		// Finally, apply the rotation and translation matrix to the current matrix
		gl.glMultMatrixf( finalMatrix, 0 );
		//////////// *** NEW *** ////////// *** NEW *** ///////////// *** NEW *** ////////////////////
		// Recursively draw the next model that is linked to the current one.
		// This could either be a body part or a gun that is attached to
		// the hand of the upper body model.
		DrawLink( pLink);
		// End the current matrix scope
		gl.glPopMatrix();
	  }
	}
  }
  //////////// *** NEW *** ////////// *** NEW *** ///////////// *** NEW *** ////////////////////

  ///////////////////////////////// SET CURRENT TIME \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
  /////
  /////	This sets time t for the interpolation between the current and next key frame
  /////
  ///////////////////////////////// SET CURRENT TIME \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

  void SetCurrentTime(t3DModel  pModel){
	double elapsedTime   = 0.0f;

	// This function is very similar to finding the frames per second.
	// Instead of checking when we reach a second, we check if we reach
	// 1 second / our animation speed. (1000 ms / animationSpeed).
	// That's how we know when we need to switch to the next key frame.
	// In the process, we get the t value for how far we are at to going to the
	// next animation key frame.  We use time to do the interpolation, that way
	// it runs the same speed on any persons computer, regardless of their specs.
	// It might look choppier on a junky computer, but the key frames still be
	// changing the same time as the other persons, it will just be not as smooth
	// of a transition between each frame.  The more frames per second we get, the
	// smoother the animation will be.  Since we are working with multiple models
	// we don't want to create static variables, so the t and elapsedTime data are
	// stored in the model's structure.

	// Return if there is no animations in this model
	if(pModel.pAnimations.size()==0)
	  return;

	// Get the current time in milliseconds
	double time = System.currentTimeMillis();

	// Find the time that has elapsed since the last time that was stored
	elapsedTime = time - pModel.lastTime;

	// Store the animation speed for this animation in a local variable
	int animationSpeed = ((pModel.pAnimations.get(pModel.currentAnim))).framesPerSecond;

	// To find the current t we divide the elapsed time by the ratio of:
	//
	// (1_second / the_animation_frames_per_second)
	//
	// Since we are dealing with milliseconds, we need to use 1000
	// milliseconds instead of 1 because we are using GetTickCount(), which is in
	// milliseconds. 1 second == 1000 milliseconds.  The t value is a value between
	// 0 to 1.  It is used to tell us how far we are from the current key frame to
	// the next key frame.
	float t =  (float)(elapsedTime / (1000.0f / animationSpeed));

	// If our elapsed time goes over the desired time segment, start over and go
	// to the next key frame.{
	if(elapsedTime >= (1000.0/animationSpeed)){
	  // Set our current frame to the next key frame (which could be the start of the anim)
	  pModel.currentFrame = pModel.nextFrame;

	  // Set our last time for the model to the current time
	  pModel.lastTime = time;
	}

	// Set the t for the model to be used in interpolation
	pModel.t = t;
  }

  //////////// *** NEW *** ////////// *** NEW *** ///////////// *** NEW *** ////////////////////

  ///////////////////////////////// RENDER MODEL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
  /////
  /////	This renders the model data to the screen
  /////
  ///////////////////////////////// RENDER MODEL \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

  void RenderModel(t3DModel pModel){

	if(pModel.pObject == null)
	  return;

	// Go through all of the objects stored in this model
	for(int i = 0; i < pModel.numOfObjects; i++){
	  // Get the current object that we are displaying
	  t3DObject pObject = pModel.pObject.get(i);
	  //////////// *** NEW *** ////////// *** NEW *** ///////////// *** NEW *** ////////////////////
	  // Now that we have animation for our model, we need to interpolate between
	  // the vertex key frames.  The .md3 file format stores all of the vertex
	  // key frames in a 1D array.  This means that in order to go to the next key frame,
	  // we need to follow this equation:  currentFrame * numberOfVertices
	  // That will give us the index of the beginning of that key frame.  We just
	  // add that index to the initial face index, when indexing into the vertex array.

	  // Find the current starting index for the current key frame we are on
	  int currentIndex = pModel.currentFrame * pObject.numOfVerts;

	  // Since we are interpolating, we also need the index for the next key frame
	  int nextIndex = pModel.nextFrame * pObject.numOfVerts;

	  //////////// *** NEW *** ////////// *** NEW *** ///////////// *** NEW *** ////////////////////

	  if(pObject.bHasTexture){
		// Turn on texture mapping
		gl.glEnable(GL.GL_TEXTURE_2D);
		// Grab the texture index from the materialID index into our material list
		int textureID = (pModel.pMaterials.get(pObject.materialID)).texureId;
		// Bind the texture index that we got from the material textureID
		gl.glBindTexture(GL.GL_TEXTURE_2D, m_Textures[textureID]);
	  }
	  else
	   // Turn off texture mapping
		gl.glDisable(GL.GL_TEXTURE_2D);

		// Start drawing our model triangles
	  gl.glBegin(GL.GL_TRIANGLES);

	  // Go through all of the faces (polygons) of the object and draw them
	  for(int j = 0; j < pObject.numOfFaces; j++){
		// Go through each vertex of the triangle and draw it.
		for(int whichVertex = 0; whichVertex < 3; whichVertex++){
		  // Get the index for the current point in the face list
		  int index = pObject.pFaces[j].vertIndex[whichVertex];
		  // Make sure there is texture coordinates for this (%99.9 likelyhood)
		  if(pObject.pTexVerts != null){
			// Assign the texture coordinate to this vertex
			gl.glTexCoord2f(pObject.pTexVerts[ index ].x,
							pObject.pTexVerts[ index ].y);
		  }
		  //////////// *** NEW *** ////////// *** NEW *** ///////////// *** NEW *** ////////////////////
		  // Like in the MD2 Animation tutorial, we use linear interpolation
		  // between the current and next point to find the point in between,
		  // depending on the model's "t" (0.0 to 1.0).
		  // Store the current and next frame's vertex by adding the current
		  // and next index to the initial index given from the face data.
		  CVector3 vPoint1 = pObject.pVerts[ currentIndex + index ];
		  CVector3 vPoint2 = pObject.pVerts[ nextIndex + index];

		  // By using the equation: p(t) = p0 + t(p1 - p0), with a time t,
		  // we create a new vertex that is closer to the next key frame.
		  gl.glVertex3f(vPoint1.x + pModel.t * (vPoint2.x - vPoint1.x),
						vPoint1.y + pModel.t * (vPoint2.y - vPoint1.y),
						vPoint1.z + pModel.t * (vPoint2.z - vPoint1.z));
		  //////////// *** NEW *** ////////// *** NEW *** ///////////// *** NEW *** ////////////////////
		}
	  }
	  // Stop drawing polygons
	  gl.glEnd();
	}
  }

  //////////// *** NEW *** ////////// *** NEW *** ///////////// *** NEW *** ////////////////////

  ///////////////////////////////// SET TORSO ANIMATION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
  /////
  ///// This sets the current animation that the upper body will be performing
  /////
  ///////////////////////////////// SET TORSO ANIMATION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

  public void SetTorsoAnimation(String strAnimation){
	// Go through all of the animations in this model
	for(int i = 0; i < m_Upper.numOfAnimations; i++){
	  // If the animation name passed in is the same as the current animation's name
	  if(((m_Upper.pAnimations.get(i))).strName.equals(strAnimation)){
		// Set the legs animation to the current animation we just found and return
		m_Upper.currentAnim = i;
		m_Upper.currentFrame = ((m_Upper.pAnimations.get(m_Upper.currentAnim))).startFrame;
		return;
	  }
	}
  }


  ///////////////////////////////// SET LEGS ANIMATION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
  /////
  /////	This sets the current animation that the lower body will be performing
  /////
  ///////////////////////////////// SET LEGS ANIMATION \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

  public void SetLegsAnimation(String strAnimation){

	// Go through all of the animations in this model
	for(int i = 0; i < m_Lower.numOfAnimations; i++){
	  // If the animation name passed in is the same as the current animation's name
	  if(((m_Lower.pAnimations.get(i))).strName.equals(strAnimation)){
		// Set the legs animation to the current animation we just found and return
		m_Lower.currentAnim = i;
		m_Lower.currentFrame = ((m_Lower.pAnimations.get(m_Lower.currentAnim))).startFrame;
		return;
	  }
	}
  }

  //////////// *** NEW *** ////////// *** NEW *** ///////////// *** NEW *** ////////////////////


  //// * NOTE * No Changes were made from the MD3 Loader code below this point * NOTE * /////

  //////////////////////////  BELOW IS THE LOADER CLASS //////////////////////////////

  ///////////////////////////////// IMPORT MD3 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
  /////
  /////	This is called by the client to open the .Md3 file, read it, then clean up
  /////
  ///////////////////////////////// IMPORT MD3 \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

  boolean ImportMD3(t3DModel pModel, String strFileName){
	FileInputStream input = null;
	int fileSize = 0;
	try{
	  input = new FileInputStream(strFileName);
	  fileSize = input.available();
	  fileContents = new byte[fileSize];
	  input.read(fileContents, 0, fileSize);
	  input.close();
	}
	catch(IOException a){
	  JOptionPane.showMessageDialog(null,"Cannot load file " + strFileName,
										 "Error",
									   JOptionPane.ERROR_MESSAGE);
	}

	//Reset
	m_FilePointer = 0;

   // Now that we know the file was found and it's all cool, let's read in
	// the header of the file.  If it has the correct 4 character ID and version number,
	// we can continue to load the rest of the data, otherwise we need to print an error.

	// Read the header data and store it in our m_Header member variable
	m_Header = new tMd3Header();

	// Get the 4 character ID
	String ID = m_Header.fileID;

	// The ID MUST equal "IDP3" and the version MUST be 15, or else it isn't a valid
	// .MD3 file.  This is just the numbers ID Software chose.
	// Make sure the ID == IDP3 and the version is this crazy number '15' or else it's a bad egg
	if(!ID.equals("IDP3") || m_Header.version != 15){
	  // Display an error message for bad file format, then stop loading
	  JOptionPane.showMessageDialog(null,"Invalid file format (Version not 15): " + strFileName,
										 "Error",
										 JOptionPane.ERROR_MESSAGE);
	  return false;
	}
	// Read in the model and animation data
	ReadMD3Data(pModel);

	// Return a success
	return true;
  }

  ///////////////////////////////// READ MD3 DATA \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
  /////
  /////	This function reads in all of the model's data, except the animation frames
  /////
  ///////////////////////////////// READ MD3 DATA \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

  void ReadMD3Data(t3DModel pModel){
	int i = 0;

	// Here we allocate memory for the bone information and read the bones in.
	m_pBones = new tMd3Bone [m_Header.numFrames];
	for(i = 0; i < m_Header.numFrames ; i++)
	  m_pBones[i] = new tMd3Bone();

	// Since we don't care about the bone positions, we just free it immediately.
	// It might be cool to display them so you could get a visual of them with the model.

	// Free the unused bones
	m_pBones = null;

	// Next, after the bones are read in, we need to read in the tags.  Below we allocate
	// memory for the tags and then read them in.  For every frame of animation there is
	// an array of tags.
	pModel.pTags = new tMd3Tag [m_Header.numFrames * m_Header.numTags];
	for(i = 0 ; i < m_Header.numFrames * m_Header.numTags ; i++)
	  pModel.pTags[i] = new tMd3Tag();

	// Assign the number of tags to our model
	pModel.numOfTags = m_Header.numTags;

	// Now we want to initialize our links.  Links are not read in from the .MD3 file, so
	// we need to create them all ourselves.  We use a double array so that we can have an
	// array of pointers.  We don't want to store any information, just pointers to t3DModels.
	pModel.pLinks = new t3DModel[m_Header.numTags];

	// Initilialize our link pointers to NULL
	for(i = 0; i < m_Header.numTags; i++)
	  pModel.pLinks[i] = null;

	// Now comes the loading of the mesh data.  We want to use ftell() to get the current
	// position in the file.  This is then used to seek to the starting position of each of
	// the mesh data arrays.

	// Get the current offset into the file
	int meshOffset = m_FilePointer;

	// Create a local meshHeader that stores the info about the mesh
	tMd3MeshInfo meshHeader = new tMd3MeshInfo();

	// Go through all of the sub-objects in this mesh
	for(int j = 0; j < m_Header.numMeshes; j++){

	  // Seek to the start of this mesh and read in it's header
	  m_FilePointer = meshOffset;
	  meshHeader = new tMd3MeshInfo();

	  // Here we allocate all of our memory from the header's information
	  m_pSkins     = new tMd3Skin [meshHeader.numSkins];
	  m_pVertices  = new tMd3Triangle [meshHeader.numVertices * meshHeader.numMeshFrames];
	  m_pTexCoords = new tMd3TexCoord [meshHeader.numVertices];
	  m_pTriangles = new tMd3Face [meshHeader.numTriangles];

	  // Read in the skin information
	  for (i = 0; i < meshHeader.numSkins ; i++)
		m_pSkins[i] = new tMd3Skin();

	  // Seek to the start of the triangle/face data, then read it in
	  m_FilePointer = meshOffset + meshHeader.triStart;
	  for (i = 0; i < meshHeader.numTriangles ; i++)
		m_pTriangles[i] = new tMd3Face();

	  // Seek to the start of the UV coordinate data, then read it in
	  m_FilePointer = meshOffset + meshHeader.uvStart;
	  for (i = 0; i < meshHeader.numVertices ; i++)
		m_pTexCoords[i] = new tMd3TexCoord();

	  // Seek to the start of the vertex/face index information, then read it in.
	  m_FilePointer = meshOffset + meshHeader.vertexStart;
	  for (i = 0; i < meshHeader.numMeshFrames * meshHeader.numVertices ; i++)
		m_pVertices[i] = new tMd3Triangle();

	  // Now that we have the data loaded into the Quake3 structures, let's convert them to
	  // our data types like t3DModel and t3DObject.  That way the rest of our model loading
	  // code will be mostly the same as the other model loading tutorials.
	  ConvertDataStructures(pModel, meshHeader);

	  // Free all the memory for this mesh since we just converted it to our structures
	  m_pSkins = null;
	  m_pVertices = null;
	  m_pTexCoords = null;
	  m_pTriangles = null;
	  // Increase the offset into the file
	  meshOffset += meshHeader.meshSize;
	}
  }

  ///////////////////////////////// CONVERT DATA STRUCTURES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
  /////
  /////	This function converts the .md3 structures to our own model and object structures
  /////
  ///////////////////////////////// CONVERT DATA STRUCTURES \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

  void  ConvertDataStructures(t3DModel pModel, tMd3MeshInfo meshHeader){

	// Increase the number of objects (sub-objects) in our model since we are loading a new one
	pModel.numOfObjects++;

	// Create a empty object structure to store the object's info before we add it to our list
	t3DObject currentMesh = new t3DObject();

	// Copy the name of the object to our object structure
	currentMesh.strName = meshHeader.strName + "";

	// Assign the vertex, texture coord and face count to our new structure
	currentMesh.numOfVerts   = meshHeader.numVertices;
	currentMesh.numOfFaces   = meshHeader.numTriangles;
	currentMesh.numTexVertex = meshHeader.numVertices;

	// Allocate memory for the vertices, texture coordinates and face data.
	// Notice that we multiply the number of vertices to be allocated by the
	// number of frames in the mesh.  This is because each frame of animation has a
	// totally new set of vertices.  This will be used in the next animation tutorial.
	currentMesh.pVerts    = new CVector3 [currentMesh.numOfVerts * meshHeader.numMeshFrames];
	currentMesh.pFaces    = new tFace [currentMesh.numOfFaces];
	currentMesh.pTexVerts = new CVector2 [currentMesh.numOfVerts];

	// Go through all of the vertices and assign them over to our structure
	for(int i=0; i < currentMesh.numOfVerts * meshHeader.numMeshFrames; i++){
	  // For some reason, the ratio 64 is what we need to divide the vertices by,
	  // otherwise the model is gargantuanly huge!  If you use another ratio, it
	  // screws up the model's body part position.  I found this out by just
	  // testing different numbers, and I came up with 65.  I looked at someone
	  // else's code and noticed they had 64, so I changed it to that.  I have never
	  // read any documentation on the model format that justifies this number, but
	  // I can't get it to work without it.  Who knows....  Maybe it's different for
	  // 3D Studio Max files verses other software?  You be the judge.  I just work here.. :)
	  currentMesh.pVerts[i]   = new CVector3();
	  currentMesh.pVerts[i].x =  m_pVertices[i].vertex[0] / 64.0f;
	  currentMesh.pVerts[i].y =  m_pVertices[i].vertex[1] / 64.0f;
	  currentMesh.pVerts[i].z =  m_pVertices[i].vertex[2] / 64.0f;
	}

	// Go through all of the uv coords and assign them over to our structure
	for(int i =0; i < currentMesh.numTexVertex; i++){
	  // Since I changed the images to bitmaps, we need to negate the V ( or y) value.
	  // This is because I believe that TARGA (.tga) files, which were originally used
	  // with this model, have the pixels flipped horizontally.  If you use other image
	  // files and your texture mapping is crazy looking, try deleting this negative.
	  currentMesh.pTexVerts[i]   = new CVector2();
	  currentMesh.pTexVerts[i].x =  m_pTexCoords[i].textureCoord[0];
	  currentMesh.pTexVerts[i].y = -m_pTexCoords[i].textureCoord[1];
	}

	// Go through all of the face data and assign it over to OUR structure
	for(int i=0; i < currentMesh.numOfFaces; i++){
	  // Assign the vertex indices to our face data
	  currentMesh.pFaces[i] =  new tFace();
	  currentMesh.pFaces[i].vertIndex[0] = m_pTriangles[i].vertexIndices[0];
	  currentMesh.pFaces[i].vertIndex[1] = m_pTriangles[i].vertexIndices[1];
	  currentMesh.pFaces[i].vertIndex[2] = m_pTriangles[i].vertexIndices[2];

	  // Assign the texture coord indices to our face data (same as the vertex indices)
	  currentMesh.pFaces[i].coordIndex[0] = m_pTriangles[i].vertexIndices[0];
	  currentMesh.pFaces[i].coordIndex[1] = m_pTriangles[i].vertexIndices[1];
	  currentMesh.pFaces[i].coordIndex[2] = m_pTriangles[i].vertexIndices[2];
	}

	// Here we add the current object to our list object list
	pModel.pObject.add(currentMesh);
  }

  ///////////////////////////////// LOAD SKIN \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
  /////
  /////	This loads the texture information for the model from the *.skin file
  /////
  ///////////////////////////////// LOAD SKIN \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

  boolean  LoadSkin(t3DModel pModel, String strSkin){

	// Make sure valid data was passed in
	if( pModel == null ||  strSkin == null ) return false;

	FileInputStream input = null;
	try{

	  input = new FileInputStream(strSkin);
	  BufferedReader reader = new BufferedReader(new InputStreamReader(input));

	  // These 2 variables are for reading in each line from the file, then storing
	  // the index of where the bitmap name starts after the last '/' character.
	  String strLine;
	  int textureNameStart = 0;
	  // Go through every line in the .skin file
	  while ((strLine = reader.readLine()) != null){
		// Loop through all of our objects to test if their name is in this line
		for(int i = 0; i < pModel.numOfObjects; i++){
		  // Check if the name of this object appears in this line from the skin file
		  if(IsInString(strLine, (pModel.pObject.get(i)).strName)){
			// To extract the texture name, we loop through the string, starting
			// at the end of it until we find a '/' character, then save that index + 1.
			textureNameStart = strLine.lastIndexOf("/") + 1;

			// Create a local material info structure
			tMaterialInfo texture = new tMaterialInfo();

			// Copy the name of the file into our texture file name variable.
			// Notice that with string we can pass in the address of an index
			// and it will only pass in the characters from that point on. Cool huh?
			// So now the strFile name should hold something like ("bitmap_name.bmp")
			texture.strFile = strLine.substring(textureNameStart);
			// The tile or scale for the UV's is 1 to 1
			texture.uTile = texture.uTile = 1;

			// Store the material ID for this object and set the texture boolean to true
			(pModel.pObject.get(i)).materialID = pModel.numOfMaterials;
			(pModel.pObject.get(i)).bHasTexture = true;
			// Here we increase the number of materials for the model
			pModel.numOfMaterials++;
			// Add the local material info structure to our model's material list
			pModel.pMaterials.add(texture);
		  }
		}
	  }
	  // Close the file and return a success
	  reader.close();
	}
	 catch (Exception e){
	 // Display the error message and return false
	  JOptionPane.showMessageDialog(null,"Unable to load Skin!",
										 "Error",
										 JOptionPane.ERROR_MESSAGE);
	  return false;
	}
	return true;
  }

	///////////////////////////////// LOAD SHADER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*
	/////
	/////	This loads the basic shader texture info associated with the weapon model
	/////
	///////////////////////////////// LOAD SHADER \\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\*

  boolean LoadShader(t3DModel  pModel, String strShader){

	// Make sure valid data was passed in
	if( pModel == null ||  strShader == null) return false;

	try{
	   BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(strShader)));

	   // These variables are used to read in a line at a time from the file, and also
	   // to store the current line being read so that we can use that as an index for the
	   // textures, in relation to the index of the sub-object loaded in from the weapon model.
	   String strLine;
	   int currentIndex = 0;

	   // Go through and read in every line of text from the file
	   while ((strLine = reader.readLine()) != null){
		 // Create a local material info structure
		 tMaterialInfo texture = new tMaterialInfo();

		 // Copy the name of the file into our texture file name variable
		 texture.strFile = strLine;

		 // The tile or scale for the UV's is 1 to 1
		 texture.uTile = texture.uTile = 1;

		 // Store the material ID for this object and set the texture boolean to true
		 (pModel.pObject.get(currentIndex)).materialID = pModel.numOfMaterials;
		 (pModel.pObject.get(currentIndex)).bHasTexture = true;

		 // Here we increase the number of materials for the model
		 pModel.numOfMaterials++;

		 // Add the local material info structure to our model's material list
		 pModel.pMaterials.add(texture);

		 // Here we increase the material index for the next texture (if any)
		 currentIndex++;
	   }
	   // Close the file and return a success
	   reader.close();
	 }
	 catch (Exception e){
	 // Display the error message and return false
	  JOptionPane.showMessageDialog(null,"Unable to load Shader!",
										 "Error",
										 JOptionPane.ERROR_MESSAGE);
	  return false;
	}
	return true;
  }

  void CreateTexture(int[] textureArray,String strFileName, int textureID){
    int[] tempArray = new int[1];
    gl.glGenTextures(1, tempArray, 0);
    textureArray[textureID] = tempArray[0];
    loadImage loader = new loadImage();
    loader.generateTextureInfo(strFileName,true);
    // This sets the alignment requirements for the start of each pixel row in memory.
    gl.glPixelStorei (GL.GL_UNPACK_ALIGNMENT, 1);
    gl.glBindTexture(GL.GL_TEXTURE_2D, textureArray[textureID]);
    //Assign the mip map levels and texture info
    gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MIN_FILTER,GL.GL_LINEAR_MIPMAP_NEAREST);
    gl.glTexParameteri(GL.GL_TEXTURE_2D,GL.GL_TEXTURE_MAG_FILTER,GL.GL_LINEAR_MIPMAP_LINEAR);
    glu.gluBuild2DMipmaps(GL.GL_TEXTURE_2D, GL.GL_RGB8, loader.width, loader.height, GL.GL_RGB, GL.GL_UNSIGNED_BYTE, loader.data);
    loader.destroy();
  }
}

/////////////////////////////////////////////////////////////////////////////////
//
// * QUICK NOTES *
//
//
// Below I will sum up everything that we went over in this tutorial.  I don't
// think it was a ton of things to sift through, but certainly model loading and
// animation are huge subjects that need a lot of code.  You can't just call
// glLoadModel() and glAnimateModel() for this stuff :)
//
// First of all, we added a basic quaternion class to our tutorial.  This is used
// to take a matrix, convert it to a quaternion, interpolate between another
// quaternion that was converted to a matrix, then turned back into a matrix.
// This is because quaternions are a great way to interpolate between rotations.
// If you wanted to use the glRotatef() and glTranslatef() functions, you would
// want to convert the interpolated quaternion to an axis angle representation,
// which might be less code, but not as useful in the long run.
//
// The next important thing that was added was the interpolation between vertex
// key frames.  We learned earlier (top of Md3.cpp) that not most of the animation
// is done through key frame vertices, not bones.  The only bone animation that is
// done is with the joints that connect the .md3 models together.  Half Life, for
// example, uses full on skeletal animation.
//
// Also, in this tutorial we added the code to parse the animation config file (.cfg).
// this stores the animation information for each animation.  The order of the are
// important.  For the most part, the config files are the same format.  As discussed
// in the previous MD3 tutorial, there is a few extra things in the config file that
// we don't read in here, such as footstep sounds and initial positions.  The tutorial
// was not designed to be a robust reusable Quake3 character loader, but to teach you
// how it works for the most part.  Other things are extra credit :)
//
// Another important thing was our timing system.  Since we were dealing with multiple
// models that had different frames per second, we needed to add some variables to our
// t3DModel class to hold some things like elapsedTime and the current t value.  This
// way, no static variables had to be created, like in the .MD2 tutorial.
//
// I would like The author of this fabulous model who allowed me to use it, his handle is:
//
//			- Pornstar (nickelbag@nyc.com).  Tasteless name, but he sure does a cool model :)
//
//
// Let me know if this helps you out!
//
//
// Ben Humphrey (DigiBen)
// Game Programmer
// DigiBen@GameTutorials.com
// Co-Web Host of www.GameTutorials.com
//
// The Quake3 .Md3 file format is owned by ID Software.  This tutorial is being used
// as a teaching tool to help understand model loading and animation.  This should
// not be sold or used under any way for commercial use with out written consent
// from ID Software.
//
// Quake, Quake2 and Quake3 are trademarks of ID Software.
// Lara Croft is a trademark of Eidos and should not be used for any commercial gain.
// All trademarks used are properties of their respective owners.

; Game Based on game in TRON
; Created By Sonic Waves TM

; Bullet functions hacked from Mark Sibly's code

; NOTE: This program's comments are
; not ment for learning purposes
; just for my future refrence.
; TOO BAD FOR N00BZ!

AppTitle "Bitstream Panic v1.26"

If CommandLine$()="walrus32.dll" Then
	Text 0,0,"Yes, Tim farted on this too."
	WaitKey()
EndIf

; Setup Graphics
Graphics3D 800,600,16,1

SeedRnd(MilliSecs())

font=LoadFont("fixedsys",24)
SetFont font

; This is here because I said so!
AutoMidHandle True

; Setup Object Types for collisions
Const Type_Player = 1, Type_Gate = 2, Type_Wall = 3, Type_Bullet = 4

; SMASH!
; Collisions
Collisions Type_Player, Type_Gate, 2, 3
Collisions Type_Gate, Type_Player, 2, 3
Collisions Type_Gate, Type_Gate, 2, 2
Collisions Type_Bullet, Type_Gate, 2, 2
Collisions Type_Bullet, Type_Wall, 2, 3

; We're going global!
; Global Variables
Global mxs, mys, mouse_shake
Global dest_cam_yaw#, dest_cam_pitch#
Global cam_yaw#, cam_pitch#
Global reload=50
Global retro=-1
Global profit=1
Global score=0
Global highscore=0
Global grab=0
Global game#=3
Global wave#=1
Global FrameWait=CreateTimer(30)

; Setup Channel Handles
Global ChnMusic
Global ChnSfx

; Load Sound Effects
Global Sfxdeepexp=LoadSound("data/DEEPEXP.WAV")
Global SfxDougheee=LoadSound("data/DOUGHEEE.WAV")
Global SfxLASERTW=LoadSound("data/LASERTW.WAV")
Global SfxPHOTON2=LoadSound("data/PHOTON2.WAV")
Global SfxREMATER=LoadSound("data/REMATER.WAV")
Global SfxMODULAT3=LoadSound("data/MODULAT3.WAV")

; Fragments
Global spark=LoadSprite( "data/spark.bmp" )
EntityAlpha spark, 0

Global one=LoadSprite( "data/one.bmp" )
EntityAlpha one, 0

; Mark's code hacked cluelessly
; Create Grid Texture
Global grid_tex=CreateTexture( 32,32,8 )
ScaleTexture grid_tex,10,10
SetBuffer TextureBuffer( grid_tex )
Color 0,0,64:Rect 0,0,32,32
Color 0,0,255:Rect 0,0,32,32,False
SetBuffer BackBuffer()

; Create The Floor
Global grid_plane=CreatePlane()
EntityTexture grid_plane,grid_tex
EntityBlend grid_plane,1
EntityAlpha grid_plane,1
EntityFX grid_plane,1
PositionEntity grid_plane, 0, -50, 0
EntityType grid_plane, Type_Wall

; End of mark's code

; Look out for that...
; Wall mesh!
Global grid_wall_A=CopyEntity(grid_plane)
PositionEntity grid_wall_A, 50,0,0
RotateEntity grid_wall_A,0,0,90
EntityAlpha grid_wall_A, 1
EntityType grid_wall_A, Type_Wall

Global grid_wall_B=CopyEntity(grid_plane)
PositionEntity grid_wall_B, -50,0,0
RotateEntity grid_wall_B,0,0,-90
EntityAlpha grid_wall_B, 1
EntityType grid_wall_B, Type_Wall

Global grid_ceiling=CopyEntity(grid_plane)
PositionEntity grid_ceiling, 0,50,0
RotateEntity grid_ceiling,0,0,180
EntityAlpha grid_ceiling, 1
EntityType grid_ceiling, Type_Wall

; FEAR THIS:
; Load Antagonist!
Global gate=LoadMesh("data/gate.3ds")
HideEntity gate

; KABOOM!
; Load Bullets
Global bulletmesh=LoadMesh("data/bullet.3ds")
HideEntity bulletmesh

; ICU!
; Create Camera
Global camera=CreateCamera()
EntityType camera, Type_Player
MoveEntity camera, 0, 5, 0
CameraClsColor camera,0,0,80
EntityRadius camera, 5

; Create Radar
Global Radar=CreateCamera(camera)
RotateEntity radar, 0,180,0
CameraClsColor radar,0,0,80
EntityRadius radar, 2
CameraViewport radar, GraphicsWidth()-(GraphicsWidth()/8), GraphicsHeight()-(GraphicsHeight()/8), GraphicsWidth()/8, GraphicsHeight()/8

; Load Images
Global gfxcrosshair = LoadImage("data/crosshair.bmp")
Global gfxlogo      = LoadImage("data/logo.bmp")

; Load Animated Images
Global gfxcoins     = LoadAnimImage("data/insert_coins.bmp",800,600,0,2)

; Setup Types
Type frags
	Field speed#, entity, alpha#
End Type

Type bit
	Field speed#, entity, alpha#
End Type

Type Bullet
	Field rot#,sprite,time_out
End Type

Type Enimy
	Field Entity,entityinside
End Type

For wert = 0 To 50
	throwbit()
Next

InsertCoin()

; WARNING: This game has been known to take
; your credit card number and use it to give
; me a quarter for each time you play this game!
; JK...

; -----------------------------------------------

; Insert Coin Loop
Function InsertCoin()
Color 255,255,255

Local coins=0

While Not coins=profit
Cls

flybits()

If Not ChannelPlaying(ChnMusic) Then
	ChnMusic=PlayMusic("data/FlyOut_Loop.wma")
EndIf

updateparticles()

For b.bullet = Each bullet
	updatebullet(b)
Next

TranslateEntity camera, 0,0,1
RotateEntity camera, 0, 0, 0

If KeyHit(1) Then End

If KeyHit(57) Then
coins=coins+1
EndIf

UpdateWorld()
RenderWorld()

delaywait=delaywait+1
If delaywait>10 Then
peek=peek+1
If peek>1 Then peek=0
delaywait=0
EndIf

DrawImage gfxlogo, GraphicsWidth()/2, GraphicsHeight()/2
DrawImage gfxcoins, GraphicsWidth()/2, GraphicsHeight()/2, peek

Text 0,0,"High Score: "+highscore
Text 0,580,"Credits: "+coins+"/"+profit
Flip

If KeyHit(88) Then
grab=grab+1
SaveBuffer FrontBuffer (), "sshot" + grab + ".bmp"
EndIf

Wend
StopChannel(ChnMusic)
game()

End Function

; -------------------------------------------------


;     **************************
;  *********** Main Loop ***********
Function Game() ; The meat's in here

mxs=0
mys=0
mouse_shake=0
dest_cam_yaw#=0
dest_cam_pitch#=0

ChnSfx=PlaySound(SfxREMATER)

If Not ChannelPlaying(ChnMusic) Then
	ChnMusic=PlayMusic("data/reachout.wma")
EndIf

spawnantagonist()

wave#=1
game#=3

While Not game#=0
Cls

flybits()

	If EntityCollided(Camera,Type_Gate) Then
		game#=game#-1
	EndIf

updateparticles()

For b.bullet = Each bullet
	updatebullet(b)
Next

controll()

updateantagonist()

; ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~    <==== Lettus (Yuck! Who put that in there?)

If KeyHit(1) Then game=ended

If retro=1 Then
retromode()
EndIf

If KeyHit(67) Then
retro=-retro
EndIf

UpdateWorld()
RenderWorld()
DrawImage gfxcrosshair, GraphicsWidth()/2, GraphicsHeight()/2

Text 0,0,"Wave: "+Int(wave#)
Text 0,12,"Life: "+Int(game)
Text 0,22,"Score: "+Score
WaitTimer(FrameWait)
Flip
Wend
;  *********************************
;     **************************

StopChannel ChnMusic

		Cls
		RenderWorld()
		ChnSfx=PlaySound(SfxMODULAT3)
		Text GraphicsWidth()/2, GraphicsHeight()/2, "GAME OVER!",1,1
		Flip
		Delay 2000

If score>highscore Then
highscore=score
EndIf

score=0

For e.enimy = Each enimy
	HideEntity e\entity
	FreeEntity e\entity
	Delete e
Next

insertcoin()

End Function


; You must learn controll!
; Controlls Function
Function Controll()

; Simon Harrison's code
; Hacked almost cluelessly
; Edited slightly so Width & Height arn't variables

	; Mouse look
	; ----------

	; Mouse x and y speed
	mxs=MouseXSpeed()
	mys=MouseYSpeed()
	
	; Mouse shake (total mouse movement)
	mouse_shake=Abs(((mxs+mys)/2)/1000.0)

	; Destination camera angle x and y values
	dest_cam_yaw#=dest_cam_yaw#-mxs
	dest_cam_pitch#=dest_cam_pitch#+mys

	; Current camera angle x and y values
	cam_yaw=cam_yaw+((dest_cam_yaw-cam_yaw)/5)
	cam_pitch=cam_pitch+((dest_cam_pitch-cam_pitch)/5)
	
	RotateEntity camera,cam_pitch#,cam_yaw#,0
	
	; Rest mouse position to centre of screen
	MoveMouse GraphicsWidth()/2,GraphicsHeight()/2

; END OF Simon's code!

; Move me foreward
; in the bitstream
TranslateEntity camera, 0,0,1

reload=reload+1

If MouseHit(1) Then
createbullet()
EndIf


If KeyHit(88) Then
grab=grab+1
SaveBuffer FrontBuffer (), "sshot" + grab + ".bmp"
EndIf

End Function

; Create Bitstream
Function throwbit()
		bt.bit = New bit
		numb=Rand(0,1)
		If numb=0 Then
		bt\entity = CopyEntity(spark)
		Else
		bt\entity = CopyEntity(one)
		EndIf
		PositionEntity bt\entity, Rand(-50,50), Rand(-50,50), EntityZ(camera)-Rand(-200,199)
		bt\speed# = Rnd(1,10)
		bt\alpha# = 1
		EntityAlpha bt\entity, bt\alpha#
		ScaleSprite bt\entity, .5, .5
End Function

Function flybits()
	For bt.bit = Each bit
		If EntityDistance(bt\entity, camera) > 250 Then
			PositionEntity bt\entity, Rand(-50,50), Rand(-50,50), EntityZ(camera)-200
		Else
			MoveEntity bt\entity, 0, 0, bt\speed#
		EndIf
	Next
End Function

; Create Bullet
Function CreateBullet.Bullet()
	ChnSfx=PlaySound(SfxPHOTON2)
	bull_x=-bull_x
	b.Bullet=New Bullet
	b\time_out=150
	b\sprite=CopyEntity( bulletmesh,camera )
	TranslateEntity b\sprite,bull_x,1,.25
	EntityParent b\sprite,0
	EntityType b\sprite, Type_Bullet
;	EmitSound shoot,b\sprite
	Return b
End Function

Function UpdateBullet( b.Bullet )
	If CountCollisions( b\sprite )
		If EntityCollided( b\sprite,Type_Wall ) Or EntityCollided( b\sprite, Type_Gate) Then
			;EmitSound boom,b\sprite
			ex#=EntityX(b\sprite)
			ey#=EntityY(b\sprite)
			ez#=EntityZ(b\sprite)
			Createparticle(ex#,ey#,ez#)
			FreeEntity b\sprite
			Delete b
			Return
		EndIf
	EndIf
	b\time_out=b\time_out-1
	If b\time_out=0
		FreeEntity b\sprite
		Delete b
		Return
	EndIf
	b\rot=b\rot+30
	TurnEntity b\sprite,0,0,b\rot
	MoveEntity b\sprite,0,0,5
End Function

Function retromode()
DrawImage gfxcrosshair, GraphicsWidth()/2, (GraphicsHeight()/2)+1
Flip
End Function

; Greate Fragments For Explosion
Function createparticle(x#,y#,z#)
	For a = 1 To 10
		f.frags = New frags
		f\entity = CopyEntity(spark)
		PositionEntity f\entity, x#, y#, z#
		f\speed# = Rnd(3,4)
		f\alpha# = 1
		RotateEntity f\entity, Rand(360), Rand(360), Rand(360)
;		EntityColor f\entity, Rand(255), Rand(255), Rand(255)
		EntityAlpha f\entity, f\alpha#
		ScaleSprite f\entity, .5, .5
	Next
End Function

; Update Explosion Fragments
Function updateparticles()
	For f.frags = Each frags
		If f\alpha# > 0
			MoveEntity f\entity, 0, 0, f\speed#
			f\alpha# = f\alpha# - 0.1
		Else
			FreeEntity f\entity
			Delete f
		EndIf
	Next
End Function

; Want a challenge?
; Creates the enimy
Function createantagonist.enimy()
	e.enimy = New enimy
	e\entity=CopyEntity(gate,e\entity)
	EntityType e\entity, Type_Gate
	RR=Rand(360)
	Dist=Rand(100,1000)
	PositionEntity e\entity,(Sin(RR)*Dist)+EntityX(camera),25,(Cos(RR)*Dist)+EntityX(camera)
	e\entityinside=CreateCube(e\entity)
	ScaleEntity e\entityinside,5,5,1
	EntityAlpha e\entityinside,0
	EntityType e\entityinside, Type_Gate
	Return e
End Function

; Want a challenge?
; Moves the enimy
Function updateantagonist()
cnt=0
	For e.enimy = Each enimy
		PointEntity e\entity, camera
		MoveEntity e\entity, 0, 0, 1.5
			If EntityCollided(e\entityinside,Type_Player) Then
				game=game-1
			EndIf
			If EntityCollided(e\entity,Type_Bullet) Then
				score=score+1000
				HideEntity e\entity
				FreeEntity e\entity
				PlaySound(Sfxdeepexp)
				Delete e
			EndIf
	cnt=cnt+1	
	Next
If cnt=0 Then
		wave#=wave#+1
		Cls
		RenderWorld()
		StopChannel(ChnSfx)
		ChnSfx=PlaySound(SfxDougheee)
		Text GraphicsWidth()/2, GraphicsHeight()/2, "Wave "+Int(wave#)+" coming up!",1,1
		Flip
		Delay 1000
		spawnantagonist()
EndIf
End Function

Function spawnantagonist()
For a = 1 To  25
createantagonist()
Next
End Function


; ********************* MORE INFORMATION *********************

; For questions and comments call
; our automated phone service at:
; 1-800-I-DONT-CARE

; To speak to a representitive call:
; 1-800-FORGET-IT
; Hours:
; 24:65 to 29:87

; Note: We are not responsible if your machine suffers
; loss of controll to the MCP!
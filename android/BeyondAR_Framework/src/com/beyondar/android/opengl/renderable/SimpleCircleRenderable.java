/*
 * Copyright (C) 2014 BeyondAR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.beyondar.android.opengl.renderable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.opengles.GL10;

import com.beyondar.android.opengl.texture.Texture;
import com.beyondar.android.util.math.geom.Plane;
import com.beyondar.android.util.math.geom.Point3;
import com.beyondar.android.util.math.geom.Vector3;
import com.beyondar.android.world.BeyondarObject;

/**
 * Basic {@link com.beyondar.android.opengl.renderable.Renderable Renderable}
 * for rendering simple 2D images.
 */
public class SimpleCircleRenderable implements Renderable {

	private static SimpleCircleRenderable mThis;

	int points = 60;
	float radius = 1.0f;
	float center_x = 0.0f;
	float center_y = 0.0f;
	float center_z = 0.0f;
	float vertices[] = new float[points*3];
	int idx = 0;
	int outerVertexCount = points-1;
	ByteBuffer byteBuffer;
	
	private Texture mTexture;
	private BeyondarObject mBeyondarObject;
	private Point3 mAngle;
	private Point3 mPosition;
	private FloatBuffer mVertexBuffer;

	private long mTimeMark;

	private SimpleCircleRenderable() {
		mAngle = new Point3();
		mPosition = new Point3();
		mTexture = new Texture();
		
		vertices[idx++] = center_x;
		vertices[idx++] = center_z;
		vertices[idx++] = center_y;
		
		for (int i = 0; i < outerVertexCount; ++i){
		    float percent = (i / (float) (outerVertexCount-1));
		    float rad = (float) (percent * 2*Math.PI);

		    //vertex position
		    float outer_x = (float) (center_x + radius * Math.cos(rad));
		    float outer_y = (float) (center_y + radius * Math.sin(rad));

		    vertices[idx++] = outer_x;
		    vertices[idx++] = center_z;
		    vertices[idx++] = outer_y;    
		}
		
		byteBuffer = ByteBuffer.allocateDirect(vertices.length * 4);
		byteBuffer.order(ByteOrder.nativeOrder());
		// allocates the memory from the byte buffer
		mVertexBuffer = byteBuffer.asFloatBuffer();
		// fill the vertexBuffer with the vertices
		mVertexBuffer.put(vertices);
		// set the cursor position to the beginning of the buffer
		mVertexBuffer.position(0);
	}

	/**
	 * Get unique instance of the {@link SimpleCircleRenderable}.
	 * 
	 * @return
	 */
	public synchronized static Renderable getInstance() {
		if (mThis == null) {
			mThis = new SimpleCircleRenderable();
		}
		return mThis;
	}

	@Override
	public boolean update(long time, double distance,
			BeyondarObject beyondarObject) {
		mTimeMark = time;
		mBeyondarObject = beyondarObject;

		mPosition.x = mBeyondarObject.getPosition().x;
		mPosition.y = mBeyondarObject.getPosition().y;
		mPosition.z = mBeyondarObject.getPosition().z;

		mAngle.x = mBeyondarObject.getAngle().x;
		mAngle.y = mBeyondarObject.getAngle().y;
		mAngle.z = mBeyondarObject.getAngle().z;

		return false;
	}

	/** The draw method for the square with the GL context */
	@Override
	public void draw(GL10 gl, Texture defaultTexture) {

//		mTexture = mBeyondarObject.getTexture();
//		Texture texture = mTexture;

		gl.glTranslatef(mPosition.x, mPosition.y, mPosition.z);

		// ROTATE According to the angles

		gl.glRotatef((float) mAngle.x, 1, 0, 0);
		gl.glRotatef((float) mAngle.y, 0, 1, 0);
		gl.glRotatef((float) mAngle.z, 0, 0, 1);

//		// bind the previously generated texture
//		if (!mTexture.isLoaded()) {
//			gl.glBindTexture(GL10.GL_TEXTURE_2D,
//					defaultTexture.getTexturePointer());
//			texture = defaultTexture;
//		} else {
//			gl.glBindTexture(GL10.GL_TEXTURE_2D, mTexture.getTexturePointer());
//		}

		// Point to our buffers
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
//		gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		// Set the face rotation
		gl.glFrontFace(GL10.GL_CW);
		gl.glDisable(GL10.GL_CULL_FACE);
		gl.glDisable(GL10.GL_TEXTURE_2D);
		gl.glEnable(GL10.GL_BLEND);
		gl.glColor4f(1f, 1f, 1f, 0.8f);

		// Point to our vertex buffer
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
//		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texture.getTextureBuffer());

		// Draw the vertices as triangle strip
//		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0,
//				vertices.length / 3);
		gl.glDrawArrays(GL10.GL_TRIANGLE_FAN, 0,
				vertices.length / 3);

		// Disable the client state before leaving
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
//		gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

		// gl.glPopMatrix();

		// rotate to the previous state
		gl.glRotatef((float) mAngle.x, -1, 0, 0);
		gl.glRotatef((float) mAngle.y, 0, -1, 0);
		gl.glRotatef((float) mAngle.z, 0, 0, -1);

		gl.glTranslatef(-mPosition.x, -mPosition.y, -mPosition.z);
	}

	@Override
	public Plane getPlane() {
		Plane plane = new Plane(mBeyondarObject.getPosition(), new Vector3(0,
				-1, 0));
		return plane;
	}

	@Override
	public void setPosition(float x, float y, float z) {
		mBeyondarObject.getPosition().x = x;
		mBeyondarObject.getPosition().y = y;
		mBeyondarObject.getPosition().z = z;
	}

	@Override
	public void setAngle(float x, float y, float z) {
		mAngle.x = x;
		mAngle.y = y;
		mAngle.z = z;
	}

	@Override
	public Point3 getAngle() {
		return this.mAngle;
	}

	@Override
	public long getTimeMark() {
		return mTimeMark;
	}

	@Override
	public Point3 getPosition() {
		if (mBeyondarObject == null) {
			return null;
		}
		return mBeyondarObject.getPosition();
	}

	@Override
	public void onNotRendered(double dst) {
	}

	@Override
	public Texture getTexture() {
		return mTexture;
	}

}

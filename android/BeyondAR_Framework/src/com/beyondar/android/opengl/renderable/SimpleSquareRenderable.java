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
public class SimpleSquareRenderable implements Renderable {

	private static SimpleSquareRenderable mThis;

	public float vertices[] = { -1.0f, 0.0f, -1.0f, // V1 - bottom left
			-1.0f, 0.0f, 1.0f, // V2 - top left
			1.0f, 0.0f, -1.0f, // V3 - bottom right
			1.0f, 0.0f, 1.0f // V4 - top right
	};
	ByteBuffer byteBuffer;
	
	private Texture mTexture;
	private BeyondarObject mBeyondarObject;
	private Point3 mAngle;
	private Point3 mPosition;
	private FloatBuffer mVertexBuffer;

	private long mTimeMark;

	private SimpleSquareRenderable() {
		mAngle = new Point3();
		mPosition = new Point3();
		mTexture = new Texture();
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
	 * Get unique instance of the {@link SimpleSquareRenderable}.
	 * 
	 * @return
	 */
	public synchronized static Renderable getInstance() {
		if (mThis == null) {
			mThis = new SimpleSquareRenderable();
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
		gl.glColor4f(0.5f, 1f, 0.5f, 1f);

		// Point to our vertex buffer
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, mVertexBuffer);
//		gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, texture.getTextureBuffer());

		// Draw the vertices as triangle strip
		gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, 0,
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

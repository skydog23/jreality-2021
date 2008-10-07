package de.jreality.scene.data;

import java.nio.FloatBuffer;

public class RingBuffer {

	private float buffer[];
	private int size;
	private int writePointer;
	
	public class Reader {
		private int readPointer;
		
		private Reader() {
			clear();
		}
		
		public int getSize() {
			return size;
		}
		
		public void clear() {
			readPointer = writePointer;
		}
		
		public int valuesLeft() {
			return (readPointer<=writePointer) ? writePointer-readPointer : size-readPointer+writePointer;
		}
		
		public int read(float target[], int initialIndex, int nValues) {
			int nLeft = valuesLeft();
			int n = (nLeft<nValues) ? nLeft : nValues;
			if (readPointer+n<size) {
				System.arraycopy(buffer, readPointer, target, initialIndex, n);
				readPointer += n;
			}
			else {
				int n1 = size-readPointer;
				System.arraycopy(buffer, readPointer, target, initialIndex, n1);
				readPointer = n-n1;
				System.arraycopy(buffer, 0, target, initialIndex+n1, readPointer);
			}
			return n;
		}
		public boolean checkBuffer(RingBuffer buf) {
			return RingBuffer.this == buf;
		}
	}
	
	public RingBuffer(int size) {
		this.size = size;
		buffer = new float[size];
		writePointer = 0;
	}
	
	public int getSize() {
		return size;
	}
	
	public Reader createReader() {
		return new Reader();
	}
	
	public void write(float source[], int initialIndex, int nSamples) {
		if (writePointer+nSamples<size) {
			System.arraycopy(source, initialIndex, buffer, writePointer, nSamples);
			writePointer += nSamples;
		}
		else {
			int n1 = size-writePointer;
			System.arraycopy(source, initialIndex, buffer, writePointer, n1);
			writePointer = nSamples-n1;
			System.arraycopy(source, initialIndex+n1, buffer, 0, writePointer);
		}
	}
	
	public void write(FloatBuffer source) {
		int nSamples = source.remaining();
		if (writePointer+nSamples<size) {
			source.get(buffer, writePointer, nSamples);
			writePointer += nSamples;
		}
		else {
			int n1 = size-writePointer;
			source.get(buffer, writePointer, n1);
			writePointer = nSamples - n1;
			source.get(buffer, 0, writePointer);
		}
	}
}
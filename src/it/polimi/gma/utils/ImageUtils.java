package it.polimi.gma.utils;

import java.io.*;

public class ImageUtils {

	public static byte[] readImage(InputStream imageInputStream) throws IOException {

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		int bytesRead = -1;

		try {
			while ((bytesRead = imageInputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}

			byte[] imageBytes = outputStream.toByteArray();
			return imageBytes;
		} catch (IOException e) {
			throw e;
		}

	}

}

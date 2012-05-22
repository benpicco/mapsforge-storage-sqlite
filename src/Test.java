import java.util.Random;

import javax.annotation.Generated;

import org.mapsforge.storage.tile.PCTilePersistenceManager;
import org.mapsforge.storage.tile.TilePersistenceManager;


public class Test {
	
	private static void generateTestFile(String file, int sizeX, int sizeY) {
		TilePersistenceManager tpm = new PCTilePersistenceManager(file);
		Random generator = new Random(1337);
		byte[] data = new byte[1024];
		
		for (int x = 0; x < sizeX; ++x) {
			for (int y = 0; y < sizeY; ++y) {
				generator.nextBytes(data);
				tpm.insertOrUpdateTile(data, x, y, (byte) 0);
				System.out.print(".");
			}
			System.out.println();
		}
		
		tpm.close();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		generateTestFile("/tmp/test.map", 64, 64);
	}

}

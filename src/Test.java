import java.util.LinkedList;
import java.util.Random;

import javax.annotation.Generated;

import org.mapsforge.map.writer.model.TileData;
import org.mapsforge.storage.tile.PCTilePersistenceManager;
import org.mapsforge.storage.tile.TileDataContainer;
import org.mapsforge.storage.tile.TilePersistenceManager;


public class Test {
	
	private static void generateTestFile(String file, int sizeX, int sizeY) {
		TilePersistenceManager tpm = new PCTilePersistenceManager(file);
		LinkedList<TileDataContainer> tdcll = new LinkedList<TileDataContainer>();
		
		Random generator = new Random(1337);
		byte[] data = new byte[1024];
		
		for (int x = 0; x < sizeX; ++x) {
			for (int y = 0; y < sizeY; ++y) {
				generator.nextBytes(data);
				tdcll.add(new TileDataContainer(data, TileDataContainer.TILE_TYPE_VECTOR, x, y, (byte) 0));
			}
		}
		
		tpm.insertOrUpdateTiles(tdcll);
		
		tpm.close();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		generateTestFile("/tmp/test.map", 64, 64);
	}

}

import java.util.LinkedList;
import java.util.Random;

import javax.annotation.Generated;

import org.mapsforge.map.writer.model.TileData;
import org.mapsforge.storage.tile.PCTilePersistenceManager;
import org.mapsforge.storage.tile.TileDataContainer;
import org.mapsforge.storage.tile.TilePersistenceManager;


public class Test {
	
	private static void generateHashTree(String file) {
		TilePersistenceManager tpm = new PCTilePersistenceManager(file);
		
		System.out.println("Hash (23,23): " + tpm.getTileHash(23, 23, (byte)0));
		
		tpm.close();
	}
		
	private static void generateTestFile(String file, int sizeX, int sizeY) {
		TilePersistenceManager tpm = new PCTilePersistenceManager(file);
		LinkedList<TileDataContainer> tdcll = new LinkedList<TileDataContainer>();
		Random generator = new Random(1337);
		
		final byte[] baseZoomLevel = tpm.getMetaData().getBaseZoomLevel();
		for (int zoomInterval = 0; zoomInterval < baseZoomLevel.length; ++zoomInterval) {
			System.out.println("zoomLevel["+zoomInterval+"]: " + baseZoomLevel[zoomInterval]);
			double div = zoomInterval > 0 ? Math.pow(2, baseZoomLevel[zoomInterval] - baseZoomLevel[zoomInterval - 1]) : 1;
		
			for (int x = 0; x < sizeX/div; ++x) {
				for (int y = 0; y < sizeY/div; ++y) {
					byte[] data = new byte[1024];
					generator.nextBytes(data);
					tdcll.add(new TileDataContainer(data, TileDataContainer.TILE_TYPE_VECTOR, x, y, (byte) zoomInterval));
				}
			}
			tpm.insertOrUpdateTiles(tdcll);
			tdcll.clear();
		}
		
		tpm.close();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final String file = "/tmp/test.map";
		
		// generateTestFile(file, 128, 128);
		
		generateHashTree(file);
	}

}

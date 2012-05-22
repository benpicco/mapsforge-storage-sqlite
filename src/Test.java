import java.util.LinkedList;
import java.util.Random;

import javax.annotation.Generated;

import org.mapsforge.map.writer.model.TileData;
import org.mapsforge.storage.tile.PCTilePersistenceManager;
import org.mapsforge.storage.tile.TileDataContainer;
import org.mapsforge.storage.tile.TilePersistenceManager;


public class Test {
	
	private static int maxDepth(int sizeX, int sizeY, int factor) {
		int depth = 1;
		
		while(Math.min(sizeX /= factor, sizeY /= factor) >= 1) 
			depth++;
		
		return depth;
	}
		
	private static void generateHashTree(String file, int sizeX, int sizeY, int factor) {
		TilePersistenceManager tpm = new PCTilePersistenceManager(file);
		
		int[][][] hashes = new int[maxDepth(sizeX, sizeY, factor)][][];
		hashes[0] = new int[sizeX][sizeY];
		
		for (int x = 0; x < sizeX; ++x)
			for (int y = 0; y < sizeY; ++y)
				hashes[0][x][y] = tpm.getTileHash(x, y, (byte) 0);
		
		tpm.close();
		
		for (int i = 1; i < hashes.length; ++i) {
			int maxX = hashes[i-1].length/factor;
			int maxY = hashes[i-1][0].length/factor;
			System.out.println("Creating Level "+i+" with dimensions "+maxX + "x" + maxY);
			hashes[i] = new int[maxX][maxY];
			
			for (int x = 0; x < maxX; ++x) {
				for (int y = 0; y < maxY; ++y) {
					int hash = 0;
					for (int n = 0; n < factor; ++n)
						for (int m = 0; m < factor; ++m)
							hash += hashes[i-1][x * factor + n][y * factor + m];
					hashes[i][x][y] = hash;
				}
			}		
		}
			
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
		
		generateHashTree(file, 128, 128, 2);
	}

}

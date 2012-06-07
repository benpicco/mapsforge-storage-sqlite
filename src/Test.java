import java.util.LinkedList;
import java.util.Random;

import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.storage.tile.PCTilePersistenceManager;
import org.mapsforge.storage.tile.TileDataContainer;
import org.mapsforge.storage.tile.TilePersistenceManager;


public class Test {
	
	private final static double E6 = 1000000.0;
	
	private static int maxDepth(double sizeX, double sizeY, int factor) {
		int depth = 1;
		
		while(Math.min(sizeX /= factor, sizeY /= factor) >= 1) 
			depth++;
		
		return depth;
	}
	
	private static int save_array(int[][] array, int x, int y) {
		return x >= array.length || y >= array[x].length ? 0 : array[x][y];
	}
		
	private static int[][][] generateHashTree(String file, int factor) {
		TilePersistenceManager tpm = new PCTilePersistenceManager(file);
		
		final byte bzl = tpm.getMetaData().getBaseZoomLevels()[0];
		final int sizeX = (int) MercatorProjection.longitudeToTileX(tpm.getMetaData().getMaxLon(), bzl);
		final int sizeY = (int) MercatorProjection.latitudeToTileY(tpm.getMetaData().getMaxLat(), bzl);
		
		System.out.println("Map Dimensions: ("+MercatorProjection.longitudeToTileX(tpm.getMetaData().getMinLon(), bzl)+" - "+sizeX+") x ("+
				MercatorProjection.latitudeToTileY(tpm.getMetaData().getMinLat(), bzl)+" - "+sizeY+")");
		
		int[][][] hashes = new int[maxDepth(sizeX, sizeY, factor)][][];
		hashes[0] = new int[sizeX][sizeY];
		
		for (int x = 0; x < sizeX; ++x)
			for (int y = 0; y < sizeY; ++y)
				hashes[0][x][y] = tpm.getTileHash(x, y, (byte) 0);
		
		tpm.close();
		
		for (int i = 1; i < hashes.length; ++i) {
			int maxX = (int) Math.ceil((double) hashes[i-1].length/factor);
			int maxY = (int) Math.ceil((double) hashes[i-1][0].length/factor);
			System.out.println("Creating Level "+i+" with dimensions "+maxX + "x" + maxY);
			hashes[i] = new int[maxX][maxY];
			
			for (int x = 0; x < maxX; ++x) {
				for (int y = 0; y < maxY; ++y) {
					int hash = 0;
					for (int n = 0; n < factor; ++n)
						for (int m = 0; m < factor; ++m)
							hash += save_array(hashes[i-1], x * factor + n, y * factor + m);
					hashes[i][x][y] = hash;
				}
			}		
		}
		
		return hashes;
	}
	
	public static void findModifiedTiles(int[][][] a, int[][][] b, int x, int y, int z, int factor) {
		
	}
		
	private static void generateTestFile(String file, int sizeX, int sizeY, boolean changeTile) {
		PCTilePersistenceManager tpm = new PCTilePersistenceManager(file);
		LinkedList<TileDataContainer> tdcll = new LinkedList<TileDataContainer>();
		Random generator = new Random(1337);
		
		final byte[] baseZoomLevel = tpm.getMetaData().getBaseZoomLevels();
		for (byte zoomInterval = 0; zoomInterval < baseZoomLevel.length; ++zoomInterval) {
			System.out.println("zoomLevel["+zoomInterval+"]: " + baseZoomLevel[zoomInterval]);
			double div = zoomInterval < baseZoomLevel.length - 1 ? Math.pow(2, baseZoomLevel[zoomInterval + 1] - baseZoomLevel[zoomInterval]) : 1;
		
			for (int x = 0; x < sizeX/div; ++x) {
				for (int y = 0; y < sizeY/div; ++y) {
					byte[] data = new byte[1024];
					generator.nextBytes(data);
					
					// DEBUG
					if(changeTile && x == 23 && y == 23)
						data = "This tile has changed!".getBytes();
					
					tdcll.add(new TileDataContainer(data, TileDataContainer.TILE_TYPE_VECTOR, x, y, zoomInterval));
					
//					if (zoomInterval > 0)
//						System.out.println(x+", " + y+", " + baseZoomLevel[zoomInterval]);
				}
			}
			System.out.println("-------");
			
			tpm.insertOrUpdateTiles(tdcll);
			tdcll.clear();
		}

		System.out.println("Map Dimensions: ("+
				MercatorProjection.longitudeToTileX(tpm.getMetaData().getMinLon() / E6, baseZoomLevel[0])
				+ " - " +
				MercatorProjection.longitudeToTileX(tpm.getMetaData().getMaxLon() / E6, baseZoomLevel[0])
				+ ") x (" +
				MercatorProjection.latitudeToTileY(tpm.getMetaData().getMinLat() / E6, baseZoomLevel[0])
				+" - "+
				MercatorProjection.latitudeToTileY(tpm.getMetaData().getMaxLat() / E6, baseZoomLevel[0])
				+")");
		
		tpm.close();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final String file = "/tmp/test.map";
		
		generateTestFile(file, 128, 128, false);
		
		System.out.println(MercatorProjection.tileYToLatitude(128, (byte) 8));
		System.out.println(MercatorProjection.tileXToLongitude(128, (byte) 8));

		System.out.println(MercatorProjection.latitudeToTileY(90, (byte) 8));

		/*
		generateTestFile(file + ".changed", 128, 128, true);
		int[][][] hashes = generateHashTree(file, 3);
				
		int[][][] hashes = generateHashTree(file, 3);
		int[][][] hashes_mod = generateHashTree(file + ".changed", 3);
		
		System.out.println((hashes.length - 1) + ": " + hashes[hashes.length-1][0][0]);
		System.out.println((hashes_mod.length - 1) + ": " + hashes_mod[hashes_mod.length-1][0][0]);
		
		System.out.println(hashes[0][23][23] + " - " + hashes_mod[0][23][23]);
		
		System.out.println(MercatorProjection.longitudeToTileX(MercatorProjection.tileXToLongitude(128, (byte) 8), (byte) 8));
		*/
	}

}

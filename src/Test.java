import java.util.List;
import java.io.File;
import java.util.LinkedList;
import java.util.Random;

import org.mapsforge.core.util.MercatorProjection;
import org.mapsforge.map.writer.model.GeoCoordinate;
import org.mapsforge.storage.tile.PCTilePersistenceManager;
import org.mapsforge.storage.tile.TileDataContainer;
import org.mapsforge.storage.tile.TilePersistenceManager;

public class Test {
		
	private static int maxDepth(double sizeX, double sizeY, int factor) {
		int depth = 1;
		
		while(Math.min(sizeX /= factor, sizeY /= factor) >= 1) 
			depth++;
		
		return depth;
	}
	
	private static int save_array(int[][] array, int x, int y) {
		return x >= array.length || y >= array[x].length ? 0 : array[x][y];
	}
		
	private static HashedTPM generateHashTree(String file, int factor) {
		TilePersistenceManager tpm = new PCTilePersistenceManager(file);
		
		final byte bzi  = (byte) (tpm.getMetaData().getBaseZoomLevels().length - 1);
		final byte bzl = tpm.getMetaData().getBaseZoomLevels()[bzi];
		final int sizeX = (int) MercatorProjection.longitudeToTileX(tpm.getMetaData().getMaxLon() / GeoCoordinate.FACTOR_DOUBLE_TO_INT, bzl);
		final int sizeY = (int) MercatorProjection.latitudeToTileY(tpm.getMetaData().getMinLat() / GeoCoordinate.FACTOR_DOUBLE_TO_INT, bzl);
		
		System.out.println("Map Dimensions: ("+
				MercatorProjection.longitudeToTileX(tpm.getMetaData().getMinLon() / GeoCoordinate.FACTOR_DOUBLE_TO_INT, bzl)
				+ " - " +
				MercatorProjection.longitudeToTileX(tpm.getMetaData().getMaxLon() / GeoCoordinate.FACTOR_DOUBLE_TO_INT, bzl)
				+ ") x (" +
				MercatorProjection.latitudeToTileY(tpm.getMetaData().getMaxLat() / GeoCoordinate.FACTOR_DOUBLE_TO_INT, bzl)
				+" - "+
				MercatorProjection.latitudeToTileY(tpm.getMetaData().getMinLat() / GeoCoordinate.FACTOR_DOUBLE_TO_INT, bzl)
				+")");
				
		int[][][] hashes = new int[maxDepth(sizeX, sizeY, factor)][][];
		hashes[hashes.length - 1] = new int[sizeX][sizeY];
		
		for (int x = 0; x < sizeX; ++x)
			for (int y = 0; y < sizeY; ++y)
				hashes[hashes.length - 1][x][y] = tpm.getTileHash(x, y, bzi);
		
		for (int i = hashes.length - 2; i >= 0; --i) {
			int maxX = (int) Math.ceil((double) hashes[i+1].length/factor);
			int maxY = (int) Math.ceil((double) hashes[i+1][0].length/factor);
			System.out.println("Creating Level "+i+" with dimensions "+maxX + "x" + maxY);
			hashes[i] = new int[maxX][maxY];
			
			for (int x = 0; x < maxX; ++x) {
				for (int y = 0; y < maxY; ++y) {
					int hash = 0;
					for (int n = 0; n < factor; ++n)
						for (int m = 0; m < factor; ++m)
							hash += save_array(hashes[i+1], x * factor + n, y * factor + m);
					hashes[i][x][y] = hash;
				}
			}
		}

		return new HashedTPM(tpm, hashes);
	}
	
	public static List<Position> findModifiedTiles(int[][][] a, int[][][] b, int startX, int startY, int z, int factor) {
		if (a.length != b.length)
			return null;
		
		List<Position> modified = new LinkedList<Position>();
		
		if(z >= a.length)
			modified.add(new Position(startX / factor, startY / factor));
		else
			for (int x = startX; x < startX + factor && x < a[z].length; ++x)
				for (int y = startY; y < startY + factor && y < a[z][x].length; ++y)
					if (a[z][x][y] != b[z][x][y])
						modified.addAll(findModifiedTiles(a, b, x * factor, y * factor, z+1, factor));
		
		return modified;
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
					if (changeTile && x == 23 && y == 23)
						data = "This tile has changed!".getBytes();
					if (changeTile && x == 23 && y == 24)
						data = "This tile has changed as well!".getBytes();
					if (changeTile && x == 100 && y == 48)
						data = "You did find me!".getBytes();
					
					tdcll.add(new TileDataContainer(data, TileDataContainer.TILE_TYPE_VECTOR, x, y, zoomInterval));
				}
			}
			
			tpm.insertOrUpdateTiles(tdcll);
			System.out.println("Written "+tdcll.size()+" tiles.");
			tdcll.clear();
		}

		System.out.println("Map Dimensions: ("+
				MercatorProjection.longitudeToTileX(tpm.getMetaData().getMinLon() / GeoCoordinate.FACTOR_DOUBLE_TO_INT, baseZoomLevel[1])
				+ " - " +
				MercatorProjection.longitudeToTileX(tpm.getMetaData().getMaxLon() / GeoCoordinate.FACTOR_DOUBLE_TO_INT, baseZoomLevel[1])
				+ ") x (" +
				MercatorProjection.latitudeToTileY(tpm.getMetaData().getMaxLat() / GeoCoordinate.FACTOR_DOUBLE_TO_INT, baseZoomLevel[1])
				+" - "+
				MercatorProjection.latitudeToTileY(tpm.getMetaData().getMinLat() / GeoCoordinate.FACTOR_DOUBLE_TO_INT, baseZoomLevel[1])
				+")");
		
		tpm.close();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final String file = "/tmp/test.map";
		
		if(!new File(file).exists())
			generateTestFile(file, 128, 128, false);
		if(!new File(file + ".changed").exists())
			generateTestFile(file + ".changed", 128, 128, true);

		HashedTPM a = generateHashTree(file, 3);
		HashedTPM b = generateHashTree(file + ".changed", 3);
		
		System.out.println("0: " + a.hashes[0][0][0]);
		System.out.println("0: " + b.hashes[0][0][0]);
		
		List<Position> modified = findModifiedTiles(a.hashes, b.hashes, 0, 0, 0, 3);
		for (Position p : modified) {
			System.out.println("Modified: " + p);
			System.out.println(new String(b.tpm.getTileData(p.tileX, p.tileY, (byte) (b.tpm.getMetaData().getAmountOfZoomIntervals() - 1))));
		}
		
		System.out.println("Done.");
	}
}

class HashedTPM {
	public TilePersistenceManager tpm;
	public int[][][] hashes;
	
	public HashedTPM(TilePersistenceManager tpm, int[][][] hashes) {
		this.tpm = tpm;
		this.hashes = hashes;
	}
}

class Position {
	public final int tileX, tileY;
	
	public Position(int x, int y) {
		this.tileX = x;
		this.tileY = y;
	}
	
	@Override
	public String toString() {
		return "(" + tileX + ", " + tileY + ")";
	}
}

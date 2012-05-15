import org.mapsforge.storage.tile.PCTilePersistenceManager;
import org.mapsforge.storage.tile.TilePersistenceManager;


public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PCTilePersistenceManager tpm = new PCTilePersistenceManager("/tmp/test.sqlite");
		
		tpm.init();
		
		tpm.insertOrUpdateTile("foobar".getBytes(), 3, (byte) 1);
		
		tpm.close();
	}

}

import java.util.List;
import java.io.File;

import org.mapsforge.storage.tile.HashTreeUtil;
import org.mapsforge.storage.util.HashedTPM;
import org.mapsforge.storage.util.Tuple;

public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		final String file = "/tmp/test.map";
		
		if(!new File(file).exists())
			HashTreeUtil.generateTestFile(file, 128, 128, false);
		if(!new File(file + ".changed").exists())
			HashTreeUtil.generateTestFile(file + ".changed", 128, 128, true);

		HashedTPM a = HashTreeUtil.generateHashTree(file, 3);
		HashedTPM b = HashTreeUtil.generateHashTree(file + ".changed", 3);
		
		System.out.println("0: " + a.hashes[0][0][0]);
		System.out.println("0: " + b.hashes[0][0][0]);
		
		List<Tuple<Integer, Integer>> modified = HashTreeUtil.findModifiedTiles(a.hashes, b.hashes, 0, 0, 0, 3);
		for (Tuple<Integer, Integer> p : modified) {
			System.out.println("Modified: " + p);
			System.out.println(new String(b.tpm.getTileData(p.a, p.b, (byte) (b.tpm.getMetaData().getAmountOfZoomIntervals() - 1))));
		}
		
		System.out.println("Done.");
	}
}

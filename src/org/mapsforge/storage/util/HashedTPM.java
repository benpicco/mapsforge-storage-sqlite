package org.mapsforge.storage.util;

import org.mapsforge.storage.tile.TilePersistenceManager;

public class HashedTPM {
	public TilePersistenceManager tpm;
	public int[][][] hashes;
	
	public HashedTPM(TilePersistenceManager tpm, int[][][] hashes) {
		this.tpm = tpm;
		this.hashes = hashes;
	}
}

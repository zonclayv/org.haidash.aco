package org.haidash.aco.model;

import java.io.File;

public interface SearchAlgorithm {

	public SearchResult search();

	public void initializeValue(final File file) throws AcoRuntimeException;
}

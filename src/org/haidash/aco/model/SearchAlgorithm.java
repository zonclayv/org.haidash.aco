package org.haidash.aco.model;

import java.io.File;
import java.io.IOException;

public interface SearchAlgorithm {

	public SearchResult search();

	public void initializeValue(final File file) throws IOException;
}

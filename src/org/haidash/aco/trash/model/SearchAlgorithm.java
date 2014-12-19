package org.haidash.aco.trash.model;

import java.io.File;

import org.haidash.aco.model.AcoRuntimeException;

public interface SearchAlgorithm {

	public SearchResult search();

	public void initializeValue(final File file) throws AcoRuntimeException;
}

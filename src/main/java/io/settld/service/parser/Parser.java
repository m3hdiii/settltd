package io.settld.service.parser;

import io.settld.model.Statistic;

/**
 * An interface for parsing different file extensions
 */
public interface Parser {

    default Statistic getStatistic(String textContent) {
        return null;
    }

    String parse();
}

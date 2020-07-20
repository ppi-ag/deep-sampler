package org.deepmock.persistence;

import org.deepmock.core.model.Behavior;

import java.util.List;

public interface SourceManager {
    void record(List<Behavior> behaviors);
    void update(List<Behavior> behaviors);
    void load(List<Behavior> behaviors);
}

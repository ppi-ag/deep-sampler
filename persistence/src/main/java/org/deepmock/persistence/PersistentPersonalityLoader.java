package org.deepmock.persistence;

import org.deepmock.core.model.Behavior;
import org.deepmock.core.model.BehaviorRepository;
import org.deepmock.core.model.ExecutionRepository;

import java.util.ArrayList;
import java.util.List;

public class PersistentPersonalityLoader {
    private final List<SourceManager> sourceManagerList = new ArrayList<>();

    public PersistentPersonalityLoader(SourceManager sourceManager) {
        addSourceProvider(sourceManager);
    }

    public PersistentPersonalityLoader source(SourceManager sourceManager) {
        addSourceProvider(sourceManager);
        return this;
    }

    public void record() {
        for (SourceManager sourceManager: sourceManagerList) {
            sourceManager.record(ExecutionRepository.getInstance().getAll());
        }
    }

    public void load() {
        for (SourceManager sourceManager: sourceManagerList) {
            List<Behavior> behaviors = sourceManager.load();

            for (Behavior behavior : behaviors) {
                BehaviorRepository.getInstance().add(behavior);
            }
        }
    }

    private void addSourceProvider(SourceManager sourceManager) {
        this.sourceManagerList.add(sourceManager);
    }

}

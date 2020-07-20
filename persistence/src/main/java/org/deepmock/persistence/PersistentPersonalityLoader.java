package org.deepmock.persistence;

import org.deepmock.core.model.BehaviorRepository;

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
            sourceManager.record(BehaviorRepository.getInstance().getCurrentExecutionBehaviors());
        }
    }

    public void update() {
        for (SourceManager sourceManager: sourceManagerList) {
            sourceManager.update(BehaviorRepository.getInstance().getCurrentExecutionBehaviors());
        }
    }

    public void load() {
        for (SourceManager sourceManager: sourceManagerList) {
            sourceManager.load(BehaviorRepository.getInstance().getCurrentExecutionBehaviors());
        }
    }

    private void addSourceProvider(SourceManager sourceManager) {
        this.sourceManagerList.add(sourceManager);
    }

}

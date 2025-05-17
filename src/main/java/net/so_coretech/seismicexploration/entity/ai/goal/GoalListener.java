package net.so_coretech.seismicexploration.entity.ai.goal;

public interface GoalListener {

    void onSucess();

    void onFailure(final String reason);
}

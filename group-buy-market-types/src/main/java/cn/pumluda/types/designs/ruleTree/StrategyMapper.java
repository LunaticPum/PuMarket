package cn.pumluda.types.designs.ruleTree;

public interface StrategyMapper<T, D, R> {

    StrategyHandler<T, D, R> get(T requestParam, D dynamicContext);

}

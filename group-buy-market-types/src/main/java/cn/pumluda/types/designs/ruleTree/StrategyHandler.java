package cn.pumluda.types.designs.ruleTree;

public interface StrategyHandler<T, D, R> {

    StrategyHandler DEFAULT = (T, D) -> {
        return null;
    };

    R apply(T requestParam, D dynamicContext) throws Exception;

}

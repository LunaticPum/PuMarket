package cn.pumluda.types.designs.ruleTree;

public abstract class AbstractStrategyRouter<T, D, R> implements StrategyHandler<T, D, R>, StrategyMapper<T, D, R> {

    protected StrategyHandler<T, D, R> defaultHandler = StrategyHandler.DEFAULT;

    public R router(T requestParam, D dynamicContext) throws Exception {
        StrategyHandler<T, D, R> StrategyHandler = get(requestParam, dynamicContext);

        if (null != StrategyHandler) {
            return StrategyHandler.apply(requestParam, dynamicContext);
        } else {
            return defaultHandler.apply(requestParam, dynamicContext);
        }
    }
}

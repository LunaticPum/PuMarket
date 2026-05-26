package cn.pumluda.types.designs.ruleTree;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

public abstract class AbstractAsyncStrategyRouter<T, D, R> implements StrategyHandler<T, D, R>, StrategyMapper<T, D, R> {

    protected StrategyHandler<T, D, R> defaultHandler = StrategyHandler.DEFAULT;

    public R router(T requestParam, D dynamicContext) throws Exception {
        StrategyHandler<T, D, R> StrategyHandler = get(requestParam, dynamicContext);

        if (null != StrategyHandler) {
            return StrategyHandler.apply(requestParam, dynamicContext);
        } else {
            return defaultHandler.apply(requestParam, dynamicContext);
        }
    }

    @Override
    public R apply(T requestParameter, D dynamicContext) throws Exception {
        multiThread(requestParameter, dynamicContext);
        return doApply(requestParameter, dynamicContext);
    }

    protected abstract void multiThread(T requestParameter, D dynamicContext) throws ExecutionException, InterruptedException, TimeoutException;

    protected abstract R doApply(T requestParameter, D dynamicContext) throws Exception;
}

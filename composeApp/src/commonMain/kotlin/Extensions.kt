import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.math.round

@Composable
expect fun <T> StateFlow<T>.collectAsStateWithLifecycle(
    context: CoroutineContext = EmptyCoroutineContext,
    initialValue: T
): State<T>


expect fun Double.getDisplayWithCurrency(currencySymbol: String): String

@Composable
expect fun BackHandler(onBack: () -> Unit)

fun Double.roundTo2Digits(): Double {
    return round(this * 100) / 100.00
}

fun Float.getDisplayWithCurrency(currencySymbol: String): String {
    return this.toDouble().getDisplayWithCurrency(currencySymbol)
}

suspend fun awaitAll(vararg blocks: suspend () -> Unit) = coroutineScope {
    blocks.forEach {
        launch { it() }
    }
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> combine(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    flow5: Flow<T5>,
    flow6: Flow<T6>,
    flow7: Flow<T7>,
    flow8: Flow<T8>,
    flow9: Flow<T9>,
    transform: suspend (T1, T2, T3, T4, T5, T6, T7, T8, T9) -> R
): Flow<R> = kotlinx.coroutines.flow.combine(
    kotlinx.coroutines.flow.combine(flow1, flow2, flow3, ::Triple),
    kotlinx.coroutines.flow.combine(flow4, flow5, flow6, ::Triple),
    kotlinx.coroutines.flow.combine(flow7, flow8, flow9, ::Triple)
) { t1, t2, t3 ->
    transform(
        t1.first,
        t1.second,
        t1.third,
        t2.first,
        t2.second,
        t2.third,
        t3.first,
        t3.second,
        t3.third
    )
}

fun <T1, T2, T3, T4, R> combine(
    flow1: Flow<T1>,
    flow2: Flow<T2>,
    flow3: Flow<T3>,
    flow4: Flow<T4>,
    transform: suspend (T1, T2, T3, T4) -> R
): Flow<R> = kotlinx.coroutines.flow.combine(
    kotlinx.coroutines.flow.combine(flow1, flow2, ::Pair),
    kotlinx.coroutines.flow.combine(flow3, flow4, ::Pair),
) { t1, t2 ->
    transform(
        t1.first,
        t1.second,
        t2.first,
        t2.second
    )
}
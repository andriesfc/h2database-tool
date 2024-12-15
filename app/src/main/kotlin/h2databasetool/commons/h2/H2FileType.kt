package h2databasetool.commons.h2

import org.h2.engine.Constants as H2EngineConstant

enum class H2FileType(val nameSuffix: String) {
    MvStoreData(H2EngineConstant.SUFFIX_MV_FILE),
    TraceData(H2EngineConstant.SUFFIX_TRACE_FILE),
    LockFile(H2EngineConstant.SUFFIX_LOCK_FILE),
    TempFile(H2EngineConstant.SUFFIX_TEMP_FILE),
    ;

    companion object
}

package h2databasetool.commons.h2

import h2databasetool.commons.h2.H2FileType.entries
import java.io.File
import org.h2.engine.Constants as H2EngineConstant

enum class H2FileType(val nameSuffix: String, val typeName: String) {
    
    MvStoreData(H2EngineConstant.SUFFIX_MV_FILE, "mv_file"),
    TraceData(H2EngineConstant.SUFFIX_TRACE_FILE, "trace_file"),
    LockFile(H2EngineConstant.SUFFIX_LOCK_FILE, "lock_file"),
    TempFile(H2EngineConstant.SUFFIX_TEMP_FILE, "temp_file"),
    ;

    companion object {
        @JvmStatic
        fun of(file: File): H2FileType? {
            val i = file.name.indexOf('.').takeUnless { it == -1 } ?: return null
            val suffix = file.name.substring(i)
            return entries.find { it.nameSuffix == suffix }
        }
    }
}

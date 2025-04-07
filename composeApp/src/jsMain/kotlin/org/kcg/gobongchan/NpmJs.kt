package org.kcg.gobongchan

import org.w3c.dom.Element

external object Papa {
    fun parse(
        csvString: String,
        config: dynamic
    ): ParseResult<dynamic>
}

// 파싱 결과 인터페이스
external interface ParseResult<T> {
    var data: Array<T>
    var meta: ParseMeta
}

// 파싱 설정 인터페이스 (미사용)
external interface ParseConfig<T> {
    var header: Boolean?
    var dynamicTyping: Boolean?
    var complete: (ParseResult<T>) -> Unit
}

external interface ParseMeta {
    var rowCount: Int
    var fields: Array<String>?
    var delimiter: String
    var linebreak: String
}

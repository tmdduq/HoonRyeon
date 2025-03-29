package org.kcg.gobongchan

external object Papa {
    fun parse(
        csvString: String,
        config: dynamic
    ): ParseResult<dynamic>
}

// 파싱 설정 인터페이스
external interface ParseConfig<T> {
    var header: Boolean?
    var dynamicTyping: Boolean?
    var complete: (ParseResult<T>) -> Unit
}

// 파싱 결과 인터페이스
external interface ParseResult<T> {
    var data: Array<T>
}

package dev.jdgarita.frnk.data.framework

/**
 * Encapsulates a response from any service.
 * [P] is the type encapsulating the parameters of the request
 * [R] is the type encapsulating the response to the request
 */
interface FrnkResponse<out P : Any, out R : Any> {
    val request: FrnkRequest<P>
    val data: R?
}

/**
 * Encapsulates a request that generated data sent to a service.
 * [P] is the type encapsulating the parameters of the request
 */
interface FrnkRequest<out P : Any> {
    val id: String
    val parameters: P
}

/**
 * fake constructor for a [FrnkResponse]
 *
 * @see FrnkResponse
 */
fun <P : Any, R : Any> FrnkResponse(request: FrnkRequest<P>, response: R?): FrnkResponse<P, R> {
    return FrnkResponseData(request, response)
}

/**
 * fake constructor for a [FrnkRequest]
 *
 * @see FrnkRequest
 */
fun <P : Any> FrnkRequest(id: String, parameters: P): FrnkRequest<P> {
    return FrnkRequestData(id, parameters)
}

private data class FrnkResponseData<out P : Any, out R : Any>(
    override val request: FrnkRequest<P>,
    override val data: R?
) : FrnkResponse<P, R>

private data class FrnkRequestData<out P : Any>(override val id: String, override val parameters: P) :
    FrnkRequest<P>
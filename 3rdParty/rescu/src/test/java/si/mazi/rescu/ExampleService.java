/**
 * Copyright (C) 2013 Matija Mazi
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package si.mazi.rescu;

import si.mazi.rescu.dto.DummyAccountInfo;
import si.mazi.rescu.dto.Order;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.math.BigDecimal;

/**
 * @author Matija Mazi
 */
@Path("api/2")
public interface ExampleService {

    @POST
    @Path("buy/")
    @Produces(MediaType.APPLICATION_JSON)
    Order buy(@FormParam("user") String user, @FormParam("password") String password, @FormParam("amount") BigDecimal amount, @FormParam("price") BigDecimal price);

    @POST
    @Path("bitcoin_withdrawal/{user}")
    @Produces(MediaType.APPLICATION_JSON)
    Object withdrawBitcoin(@PathParam("user") String user, @FormParam("password") String password, @QueryParam("amount") BigDecimal amount, @QueryParam("address") String address);

    @GET
    @Path("{ident}_{currency}/ticker")
    Object getTicker(@PathParam("ident") String tradeableIdentifier, @PathParam("currency") String currency);

    @POST
    @FormParam("method")
    Object getInfo(Long from, Long count) throws ExampleException;

    @GET
    @Path("auth")
    Object testBasicAuth(@HeaderParam("Authorization") BasicAuthCredentials credentials, @QueryParam("param") Integer value);

    @POST
    @Path("json")
    @Consumes(MediaType.APPLICATION_JSON)
    Object testJsonBody(DummyAccountInfo ticker);
}

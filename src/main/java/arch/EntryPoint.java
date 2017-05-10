package arch;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public class EntryPoint {
	@GET
    public String get(){
       return "hello";
    }
}

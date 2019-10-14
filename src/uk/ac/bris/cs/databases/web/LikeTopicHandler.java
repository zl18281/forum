package uk.ac.bris.cs.databases.web;

import fi.iki.elonen.NanoHTTPD;
import uk.ac.bris.cs.databases.api.APIProvider;
import uk.ac.bris.cs.databases.api.Result;

/**
 *
 * @author csxdb
 */
public class LikeTopicHandler extends RPHandler {

    @Override
    RenderPair doRender(String p,
                        NanoHTTPD.IHTTPSession session)
                        throws RenderException {
        
        NanoHTTPD.CookieHandler h = session.getCookies();
        String username = h.read("user");
        if (username == null || username.equals("")) {
            return new RenderPair(null, Result.failure(
                "You must log in to like topics. " +
                "Select 'list of users' in the top bar."));
        }
        
        int id = Integer.parseInt(p);
        
        APIProvider api = ApplicationContext.getInstance().getApi();
        Result r = api.likeTopic(username, id, true);
        if (r.isSuccess()) {
            return new RenderPair("Success.ftl", 
                Result.success(new ValueHolder("Success.")));
        } else {
            return new RenderPair(null, r);
        }
    }
}

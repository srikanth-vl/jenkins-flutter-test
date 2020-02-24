package global.com.vassarlabs.play.global;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.vassarlabs.proj.uniapp.launch.test.UniAppLaunchService;

import play.Application;
import play.GlobalSettings;
import play.api.mvc.EssentialFilter;
import play.filters.gzip.GzipFilter;
import play.libs.F.Promise;
import play.mvc.Action;
import play.mvc.Http;
import play.mvc.Result;

public class Global extends GlobalSettings {

	private ApplicationContext ctx;

	@SuppressWarnings("unchecked")
	public <T extends EssentialFilter> Class<T>[] filters() {
        return new Class[]{GzipFilter.class};
    }
    

	@Override
	public void onStart(Application app) {
		ApplicationContext backendCtx = new AnnotationConfigApplicationContext(UniAppLaunchService.class);
		ctx = new ClassPathXmlApplicationContext(new String[] {"components.xml"}, backendCtx);
	}

	@Override
	public <A> A getControllerInstance(Class<A> clazz) {
		System.out.println("in getControllerInstance clazz ="+clazz);
		System.out.println("in getControllerInstance getBean(clazz) ="+ctx.getBean(clazz));
		return ctx.getBean(clazz);
	}
	
	// For CORS
	  private class ActionWrapper extends Action.Simple {
	    public ActionWrapper(Action<?> action) {
	      this.delegate = action;
	    }

	    @Override
	    public Promise<Result> call(Http.Context ctx) throws java.lang.Throwable {
	      Promise<Result> result = this.delegate.call(ctx);
	      Http.Response response = ctx.response();
	      System.out.println("In Global app"+ctx.request());
	      System.out.println("In Global app"+ctx.response());
	      
	      
	      response.setHeader("Allow", "*");
	      response.setHeader("Access-Control-Allow-Origin", "*");
	      response.setHeader("Access-Control-Allow-Credentials","true");
	      response.setHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE");
	    
	      if( ctx.request().getHeader("compId") != null){
	    	  response.setHeader("compId", ctx.request().getHeader("compId"));
	      }
	      
	      response.setHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept, Referer, User-Agent, Authorization, Authentication,compId");
	      
	      
	      return result;
	    }
	  }

	  @Override
	  public Action<?> onRequest(Http.Request request,
	      java.lang.reflect.Method actionMethod) {
	    return new ActionWrapper(super.onRequest(request, actionMethod));
	  }
	  
	  public static ByteArrayOutputStream gzip(final String input)
	            throws IOException {
	        final InputStream inputStream = new ByteArrayInputStream(input.getBytes());
	        final ByteArrayOutputStream stringOutputStream = new ByteArrayOutputStream((int) (input.length() * 0.75));
	        final OutputStream gzipOutputStream = new GZIPOutputStream(stringOutputStream);

	        final byte[] buf = new byte[5000];
	        int len;
	        while ((len = inputStream.read(buf)) > 0) {
	            gzipOutputStream.write(buf, 0, len);
	        }

	        inputStream.close();
	        gzipOutputStream.close();

	        return stringOutputStream;
	    }
}

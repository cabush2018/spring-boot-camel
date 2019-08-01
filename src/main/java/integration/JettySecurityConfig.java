package integration;

import org.springframework.context.annotation.Configuration;

@Configuration
public class JettySecurityConfig {
//	@Value("${integration.api.path}")
//	public String contextPath;
//
//	@Bean
//	@ConditionalOnClass(org.eclipse.jetty.server.Server.class)
//	SecurityHandler securityHandler() {
//		Constraint constraint = new Constraint(Constraint.__BASIC_AUTH, "client");
//		constraint.setAuthenticate(true);
//
//		ConstraintMapping mapping = new ConstraintMapping();
//		mapping.setConstraint(constraint);
//		mapping.setPathSpec(contextPath + "/*");
//		
//		ConstraintSecurityHandler handler = new ConstraintSecurityHandler();
//		
//		handler.addConstraintMapping(mapping);
//		handler.setAuthenticator(new BasicAuthenticator());
//		handler.setLoginService(new LoginService() {
//		    @SuppressWarnings("rawtypes")
//			private final Map users = new ConcurrentHashMap();
//
//		    // matches what is in the constraint object in the spring config
//		    private final String[] ACCESS_ROLE = new String[] { "rolename" };
//			
//		    private IdentityService identityService = new DefaultIdentityService();
//
//			@Override
//			public IdentityService getIdentityService() {
//				return identityService;
//			}
//
//			@Override
//			public String getName() {
//				return "";
//			}
//
//			@SuppressWarnings("unchecked")
//			@Override
//			public UserIdentity login(String username, Object creds, ServletRequest request) {
//				
//		        UserIdentity user = null;
//		        
//				boolean validUser = "ralph".equals(username) && "s3cr3t".equals(creds);
//				if (validUser) {
//					//Credential credential = (creds instanceof Credential)?(Credential)creds:Credential.getCredential(creds.toString());
//
//				    Principal userPrincipal = new Principal() {
//						
//						@Override
//						public String getName() {
//							return username;
//						}
//					};//new MappedLoginService.KnownUser(username,credential);
//					
//				    Subject subject = new Subject();
//				    subject.getPrincipals().add(userPrincipal);
//				    subject.getPrivateCredentials().add(creds);
//				    subject.setReadOnly();
//				    user=identityService.newUserIdentity(subject,userPrincipal, ACCESS_ROLE);
//				    users.put(user.getUserPrincipal().getName(), true);
//				}
//
//			    return (user != null) ? user : null;
//			}
//
//			@Override
//			public void logout(UserIdentity arg0) {
//				
//			}
//
//			@Override
//			public void setIdentityService(IdentityService arg0) {
//			     this.identityService = arg0;
//				
//			}
//
//			@Override
//			public boolean validate(UserIdentity user) {
//				if (users.containsKey(user.getUserPrincipal().getName()))
//		            return true;
//
//		        return false;	
//			}		});
//		
//		return handler;
//	}
//
//
}
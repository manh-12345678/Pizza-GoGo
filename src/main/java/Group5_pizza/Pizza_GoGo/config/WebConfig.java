package Group5_pizza.Pizza_GoGo.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ManagerAuthorizationInterceptor managerAuthorizationInterceptor;
    private final RememberMeInterceptor rememberMeInterceptor;

    @Autowired
    public WebConfig(ManagerAuthorizationInterceptor managerAuthorizationInterceptor,
                     RememberMeInterceptor rememberMeInterceptor) {
        this.managerAuthorizationInterceptor = managerAuthorizationInterceptor;
        this.rememberMeInterceptor = rememberMeInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Remember Me interceptor - chạy trước để tự động đăng nhập
        registry.addInterceptor(rememberMeInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/login",
                        "/login/**",
                        "/register",
                        "/register/**",
                        "/logout",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/static/**",
                        "/error");
        
        // Manager authorization interceptor
        registry.addInterceptor(managerAuthorizationInterceptor)
                .addPathPatterns(
                        "/manager/**",
                        "/accounts/**",
                        "/products/manage/**",
                        "/products/add/**",
                        "/products/edit/**",
                        "/products/delete/**",
                        "/combos/manage/**",
                        "/combos/add/**",
                        "/combos/edit/**",
                        "/combos/delete/**",
                        "/combos/view/**",
                        "/toppings/manage/**",
                        "/toppings/add/**",
                        "/toppings/edit/**",
                        "/toppings/delete/**",
                        "/ingredients/manage/**",
                        "/ingredients/add/**",
                        "/ingredients/edit/**",
                        "/ingredients/delete/**",
                        "/vouchers/manage/**",
                        "/vouchers/add/**",
                        "/vouchers/edit/**",
                        "/vouchers/delete/**",
                        "/vouchers/activate/**",
                        "/vouchers/deactivate/**",
                        "/categories/**",
                        "/tables/**",
                        "/reviews/manage",
                        "/reviews/manager/**",
                        "/reviews/delete/**")
                .excludePathPatterns(
                        "/products/api/**",
                        "/combos/api/**",
                        "/ingredients/api/**",
                        "/reviews/api/**");
    }
}


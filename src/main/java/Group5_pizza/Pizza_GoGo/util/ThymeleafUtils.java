package Group5_pizza.Pizza_GoGo.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

@Component
public class ThymeleafUtils {

    private final SpringTemplateEngine templateEngine;

    @Autowired
    public ThymeleafUtils(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String renderFragment(String fragmentExpression, Map<String, Object> variables) {
        try {
            Context context = new Context();
            if (variables != null) {
                context.setVariables(variables);
            }
            // Thymeleaf fragment expression format: "template :: fragment"
            // Template path should be relative to templates/ folder
            // If fragmentExpression doesn't contain ::, assume it's a full template path
            String expression = fragmentExpression;
            if (!expression.contains("::")) {
                // If no fragment specified, assume we want the whole template
                expression = expression + " :: content";
            }
            return templateEngine.process(expression, context);
        } catch (Exception e) {
            e.printStackTrace();
            return "<!-- Error rendering fragment: " + e.getMessage() + " -->";
        }
    }
}
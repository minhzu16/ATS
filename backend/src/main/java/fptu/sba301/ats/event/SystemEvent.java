package fptu.sba301.ats.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class SystemEvent extends ApplicationEvent {

    private final String title;
    private final String content;
    private final String type;
    private final String link;

    public SystemEvent(Object source, String title, String content, String type, String link) {
        super(source);
        this.title = title;
        this.content = content;
        this.type = type;
        this.link = link;
    }
}

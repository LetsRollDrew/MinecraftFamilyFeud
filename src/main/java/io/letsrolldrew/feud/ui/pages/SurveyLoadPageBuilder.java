package io.letsrolldrew.feud.ui.pages;

import static io.letsrolldrew.feud.ui.BookUiComponents.page;
import static io.letsrolldrew.feud.ui.BookUiComponents.row3;
import static io.letsrolldrew.feud.ui.BookUiComponents.spacerLine;

import io.letsrolldrew.feud.survey.Survey;
import io.letsrolldrew.feud.survey.SurveyRepository;
import io.letsrolldrew.feud.ui.BookButtonFactory;
import io.letsrolldrew.feud.ui.HostBookPage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class SurveyLoadPageBuilder {
    private final SurveyRepository surveyRepository;
    private final BookButtonFactory buttons;

    public SurveyLoadPageBuilder(SurveyRepository surveyRepository, BookButtonFactory buttons) {
        this.surveyRepository = surveyRepository;
        this.buttons = Objects.requireNonNull(buttons, "buttons");
    }

    public Component build(Survey activeSurvey) {
        if (surveyRepository == null || surveyRepository.listAll().isEmpty()) {
            return page(
                    Component.text("Survey Selection List", NamedTextColor.GOLD),
                    spacerLine(),
                    Component.text("No surveys found. Use /feud survey list."));
        }

        List<Survey> surveys = surveyRepository.listAll();
        List<Component> rows = new ArrayList<>();

        rows.add(Component.text("Survey Selection List", NamedTextColor.GOLD));
        rows.add(spacerLine());

        for (int i = 0; i < surveys.size(); i += 3) {
            Survey s1 = surveys.get(i);
            Survey s2 = (i + 1) < surveys.size() ? surveys.get(i + 1) : null;
            Survey s3 = (i + 2) < surveys.size() ? surveys.get(i + 2) : null;

            Component c1 = surveyButton(s1, activeSurvey);
            Component c2 = s2 == null ? Component.text(" ") : surveyButton(s2, activeSurvey);
            Component c3 = s3 == null ? Component.text(" ") : surveyButton(s3, activeSurvey);

            rows.add(row3(c1, c2, c3));
        }

        return page(rows.toArray(new Component[0]));
    }

    private Component surveyButton(Survey survey, Survey activeSurvey) {
        String command = "/feud survey load " + survey.id();
        String label = survey.displayName();

        NamedTextColor color = activeSurvey != null && survey.id().equals(activeSurvey.id())
                ? NamedTextColor.GREEN
                : NamedTextColor.BLUE;

        return buttons.runCommand(HostBookPage.SURVEYS, label, command, survey.question(), color, false);
    }
}

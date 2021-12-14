package de.qualityminds.gta.driver.utils.serenity;

import static net.thucydides.core.steps.StepEventBus.getEventBus;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import net.thucydides.core.model.TestStep;
import net.thucydides.core.model.TestTag;

import com.google.common.collect.Lists;
import net.serenitybdd.core.Serenity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SerenityReport {
	private static final Logger logger = LoggerFactory.getLogger(SerenityReport.class);
	private static final String HTML_SYMBOL_COLLAPSED = "&#11166;";
	private static final String HTML_SYMBOL_EXPANDED = "&#11167;";

	private SerenityReport() {
		throw new IllegalStateException("Utility class");
	}


	public static void addTagToCurrentTest(String tagValue) {
		if (getEventBus().isBaseStepListenerRegistered()) {
			getEventBus().addTagsToCurrentTest(Collections.singletonList(TestTag.withName(tagValue).andType("tag")));
		} else {
			logger.warn("Unable to write tags to report: StepListener not connected");
		}

	}

	public static void makeTableToggleable() {
		if (getEventBus().isBaseStepListenerRegistered()) {
			getEventBus().getBaseStepListener().latestTestOutcome().ifPresent(outcome -> {
				TestStep step = outcome.lastStep();
				List<String> description = Lists.newArrayList(step.getDescription().split("\n"));
				if (description.size() > 1) {
					// split step description in parts
					String stepWithoutTable = description.get(0);
					List<String> gherkinTable = description.subList(1, description.size());

					// new step description
					step.setDescription(stepWithoutTable);

					//inject content by unescaped title; inject HTML tag by UUID
					String id = UUID.randomUUID().toString();
					Serenity.recordReportData()
							.withTitle(makeToggleButtonAndInjectTable(id, gherkinTable))
							.andContents(id);
				} else {
					logger.error("Tried to make toggleable table but step description has no table: {}", description);
				}
			});
		} else {
			logger.warn("Table was not hidden in Serenity report: StepListener not connected");
		}
	}

	private static String makeToggleButtonAndInjectTable(String uniqueId, List<String> gherkinTable) {
		String htmlTable = "<table class=\"embedded\"><tbody><tr><td>" +
				gherkinTable.stream()
						.map(it -> it.replace(" | ", "</td><td>").replace("|", "").replace("'", "\\'").trim())
						.collect(Collectors.joining("</td></tr><tr><td>")) +
				"</td></tr></tbody></table>";
		return HTML_SYMBOL_COLLAPSED +
				"<script>" +
				"window.addEventListener('load',(event)=>{" +
				// toggle button symbol on expand<->collapse
				"$(\"span.piece-of-evidence:has(pre:contains('" + uniqueId + "')) button\").on('click',(event)=>{if(event.target.getAttribute('aria-expanded')==='true'){event.target.innerHTML='" + HTML_SYMBOL_COLLAPSED + "';}else{event.target.innerHTML='" + HTML_SYMBOL_EXPANDED + "';};});" +
				// inject table
				"$(\".multi-collapse:has(pre:contains('" + uniqueId + "'))\").html('<pre>" + htmlTable + "</pre>');" +
				"});" +
				"</script>";
	}
}

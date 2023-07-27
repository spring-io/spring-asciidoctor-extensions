/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.asciidoctor.blockswitch;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import org.asciidoctor.Asciidoctor;
import org.asciidoctor.Attributes;
import org.asciidoctor.Options;
import org.asciidoctor.SafeMode;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AssertDelegateTarget;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.logging.LogEntries;
import org.openqa.selenium.logging.LogType;
import org.openqa.selenium.logging.LoggingPreferences;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testcontainers.containers.BrowserWebDriverContainer;
import org.testcontainers.images.builder.Transferable;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the block switch extension.
 *
 * @author Andy Wilkinson
 */
@Testcontainers(disabledWithoutDocker = true)
public class BlockSwitchIntegrationTests {

	@Container
	private final BrowserWebDriverContainer<?> chrome = new BrowserWebDriverContainer<>()
		.withCapabilities(chromeOptions());

	static ChromeOptions chromeOptions() {
		ChromeOptions chromeOptions = new ChromeOptions();
		LoggingPreferences loggingPreferences = new LoggingPreferences();
		loggingPreferences.enable(LogType.BROWSER, Level.ALL);
		chromeOptions.setCapability(CapabilityType.LOGGING_PREFS, loggingPreferences);
		return chromeOptions;
	}

	@Test
	void singleSwitchIsCreated() throws IOException {
		RemoteWebDriver driver = load("singleSwitch.adoc");
		assertThat(driver.manage().logs().get(LogType.BROWSER)).isEmpty();
		List<WebElement> listings = driver.findElementsByCssSelector(".listingblock.primary");
		assertThat(listings).hasSize(1);
		assertThat(switchBlock(listings.get(0))).hasSelectedItem("Alpha")
			.hasUnselectedItems("Bravo")
			.hasDisplayedContentContaining("Alpha 1");
	}

	@Test
	void multipleSwitchesWithSameOptionsAreCreated() throws IOException {
		RemoteWebDriver driver = load("multipleSwitchesSameOptions.adoc");
		assertThat(driver.manage().logs().get(LogType.BROWSER)).isEmpty();
		List<WebElement> listings = driver.findElementsByCssSelector(".listingblock.primary");
		assertThat(listings).hasSize(2)
			.allSatisfy((element) -> assertThat(switchBlock(element)).hasSelectedItem("Alpha")
				.hasUnselectedItems("Bravo")
				.hasDisplayedContentContaining("Alpha"));
	}

	@Test
	void multipleSwitchesWithDifferentOptionsAreCreated() throws IOException {
		RemoteWebDriver driver = load("multipleSwitchesDifferentOptions.adoc");
		assertThat(driver.manage().logs().get(LogType.BROWSER)).isEmpty();
		List<WebElement> listings = driver.findElementsByCssSelector(".listingblock.primary");
		assertThat(listings).hasSize(2);
		assertThat(switchBlock(listings.get(0))).hasSelectedItem("Alpha")
			.hasUnselectedItems("Bravo")
			.hasDisplayedContentContaining("Alpha");
		assertThat(switchBlock(listings.get(1))).hasSelectedItem("Charlie")
			.hasUnselectedItems("Delta", "Echo")
			.hasDisplayedContentContaining("Charlie 1");
	}

	@Test
	void givenASingleSwitchWhenUnselectedItemIsClickedThenItBecomesSelected() throws IOException {
		RemoteWebDriver driver = load("singleSwitch.adoc");
		assertThat(driver.manage().logs().get(LogType.BROWSER)).isEmpty();
		List<WebElement> listings = driver.findElementsByCssSelector(".listingblock.primary");
		assertThat(listings).hasSize(1);
		assertThat(switchBlock(listings.get(0))).hasSelectedItem("Alpha")
			.hasUnselectedItems("Bravo")
			.hasDisplayedContentContaining("Alpha 1")
			.uponClicking("Bravo")
			.hasSelectedItem("Bravo")
			.hasUnselectedItems("Alpha")
			.hasDisplayedContentContaining("Bravo 1");
	}

	@Test
	void givenASingleSwitchWithCalloutsWhenUnselectedItemIsClickedThenItBecomesSelectedAndItsCalloutsBecomeVisible()
			throws IOException {
		RemoteWebDriver driver = load("singleSwitchWithCallouts.adoc");
		assertThat(driver.manage().logs().get(LogType.BROWSER)).isEmpty();
		List<WebElement> listings = driver.findElementsByCssSelector(".listingblock.primary");
		assertThat(listings).hasSize(1);
		assertThat(switchBlock(listings.get(0))).hasSelectedItem("Alpha")
			.hasUnselectedItems("Bravo")
			.hasDisplayedContentContaining("Alpha 1")
			.hasDisplayedCalloutListContaining("Alpha callout")
			.uponClicking("Bravo")
			.hasSelectedItem("Bravo")
			.hasUnselectedItems("Alpha")
			.hasDisplayedCalloutListContaining("Bravo callout");
	}

	@Test
	void givenMultipleSwitchesWithTheSameOptionsWhenUnselectedItemIsClickedThenItBecomesSelected() throws IOException {
		RemoteWebDriver driver = load("multipleSwitchesSameOptions.adoc");
		assertThat(driver.manage().logs().get(LogType.BROWSER)).isEmpty();
		List<WebElement> listings = driver.findElementsByCssSelector(".listingblock.primary");
		assertThat(listings).hasSize(2);
		assertThat(switchBlock(listings.get(1))).hasSelectedItem("Alpha")
			.hasUnselectedItems("Bravo")
			.hasDisplayedContentContaining("Alpha 2");
		assertThat(switchBlock(listings.get(0))).hasSelectedItem("Alpha")
			.hasUnselectedItems("Bravo")
			.hasDisplayedContentContaining("Alpha 1")
			.uponClicking("Bravo")
			.hasSelectedItem("Bravo")
			.hasUnselectedItems("Alpha")
			.hasDisplayedContentContaining("Bravo 1");
		assertThat(switchBlock(listings.get(1))).hasSelectedItem("Bravo")
			.hasUnselectedItems("Alpha")
			.hasDisplayedContentContaining("Bravo 2");
	}

	@Test
	void givenMultipleSwitchesWithDifferentOptionsWhenUnselectedItemIsClickedThenItBecomesSelected()
			throws IOException {
		RemoteWebDriver driver = load("multipleSwitchesDifferentOptions.adoc");
		assertThat(driver.manage().logs().get(LogType.BROWSER)).isEmpty();
		List<WebElement> listings = driver.findElementsByCssSelector(".listingblock.primary");
		assertThat(listings).hasSize(2);
		assertThat(switchBlock(listings.get(1))).hasSelectedItem("Charlie")
			.hasUnselectedItems("Delta", "Echo")
			.hasDisplayedContentContaining("Charlie 1");
		assertThat(switchBlock(listings.get(0))).hasSelectedItem("Alpha")
			.hasUnselectedItems("Bravo")
			.hasDisplayedContentContaining("Alpha 1")
			.uponClicking("Bravo")
			.hasSelectedItem("Bravo")
			.hasUnselectedItems("Alpha")
			.hasDisplayedContentContaining("Bravo 1");
		assertThat(switchBlock(listings.get(1))).hasSelectedItem("Charlie")
			.hasUnselectedItems("Delta", "Echo")
			.hasDisplayedContentContaining("Charlie 1");
	}

	@Test
	void secondaryBlockWithNoPrimary() throws IOException {
		RemoteWebDriver driver = load("secondaryBlockWithNoPrimary.adoc");
		LogEntries browserLogs = driver.manage().logs().get(LogType.BROWSER);
		assertThat(browserLogs).hasSize(1);
		assertThat(browserLogs.iterator().next().getMessage())
			.endsWith("\"Found secondary block with no primary sibling\"");
		List<WebElement> primaries = driver.findElementsByCssSelector(".listingblock.primary");
		assertThat(primaries).hasSize(0);
		List<WebElement> secondaries = driver.findElementsByCssSelector(".listingblock.secondary");
		assertThat(secondaries).hasSize(1);
	}

	private RemoteWebDriver load(String adocFile) throws IOException {
		Options options = options();
		options.setHeaderFooter(true);
		options.setSafe(SafeMode.SERVER);
		Attributes attributes = attributes();
		attributes.setAttribute("!webfonts", null);
		options.setAttributes(attributes);
		String converted = Asciidoctor.Factory.create()
			.convert(new String(Files.readAllBytes(Paths.get("src", "test", "resources", adocFile)),
					StandardCharsets.UTF_8), options);
		this.chrome.copyFileToContainer(Transferable.of(converted.getBytes(StandardCharsets.UTF_8)), "/test.html");
		RemoteWebDriver driver = this.chrome.getWebDriver();
		driver.get("file:///test.html");
		return driver;
	}

	@SuppressWarnings("deprecation")
	private Options options() {
		return new Options();
	}

	@SuppressWarnings("deprecation")
	private Attributes attributes() {
		return new Attributes();
	}

	private SwitchBlockAssert switchBlock(WebElement element) {
		return new SwitchBlockAssert(element);
	}

	private static class SwitchBlockAssert extends AbstractAssert<SwitchBlockAssert, WebElement>
			implements AssertDelegateTarget {

		SwitchBlockAssert(WebElement actual) {
			super(actual, SwitchBlockAssert.class);
		}

		SwitchBlockAssert hasSelectedItem(String item) {
			List<WebElement> selected = this.actual.findElements(By.cssSelector(".switch--item.selected"));
			assertThat(selected).extracting(WebElement::getText).containsExactly(item);
			return this;
		}

		SwitchBlockAssert hasUnselectedItems(String... items) {
			List<WebElement> unselected = this.actual.findElements(By.cssSelector(".switch--item:not(.selected)"));
			assertThat(unselected).extracting(WebElement::getText).containsExactly(items);
			return this;
		}

		SwitchBlockAssert hasDisplayedContentContaining(String contained) {
			WebElement content = this.actual.findElement(By.cssSelector(".content:not(.hidden)"));
			assertThat(content.isDisplayed()).isTrue();
			assertThat(content.getText()).contains(contained);
			return this;
		}

		SwitchBlockAssert hasDisplayedCalloutListContaining(String contained) {
			WebElement content = this.actual.findElement(By.cssSelector(".content:not(.hidden)"))
				.findElement(By.cssSelector(".colist"));
			assertThat(content.isDisplayed()).isTrue();
			assertThat(content.getText()).contains(contained);
			return this;
		}

		SwitchBlockAssert uponClicking(String item) {
			List<WebElement> unselected = this.actual.findElements(By.cssSelector(".switch--item:not(.selected)"));
			Optional<WebElement> match = unselected.stream()
				.filter((element) -> element.getText().equals(item))
				.findFirst();
			assertThat(match).isPresent();
			match.get().click();
			return this;
		}

	}

}

package de.qualityminds.gta.webapplication.pageobjects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.lang.Nullable;

public abstract class BaseElement {
    WebDriver driver;
    WebElement parent;
    WebElement element;

    protected BaseElement(WebDriver driver, By by, @Nullable WebElement parentElement) {
        this.driver = driver;
        this.parent = parentElement;
        this.element = parentElement != null ? parentElement.findElement(by) : driver.findElement(by);
    }

    public WebElement findElement(By by) {
        return element.findElement(by);
    }
}

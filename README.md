### Pref Data – the Android SharedPreferences wrapper

#### HelloWorld
 1. Create a class and annotate it with ```@PrefData```
```java
@PrefData
public abstract class UserSettings {
    // the fields must be protected or package private
    int age;
    String name;
}
```
 2. Create an instance of the generated class (it will be prefixed by "Prefs")

```java
SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
PrefUserSettings userSettings = new PrefUserSettings(prefs);
```
#### Download
```groovy
apt 'org.xelevra.libs:prefdata-processor:2.0'
provided 'org.xelevra.libs:prefdata-annotations:2.0'
```

#### Usage
###### Get and Set
```java
int age = userSettings.getAge();
userSettings.setAge(18);
```
###### Chains
```java
userSettings.setAge(18).setName("Stephen");
```
###### Default value
Initial value of your fields will be default values.
```java
int age = 18;
```
###### Clear
For clearing the preferences use ```clear()```
###### Support remove
Mark the class with ```@GenerateRemove``` and ```remove``` methods will be generated
```java
userSettings.removeAge();
```
###### Prefix
Mark your field with ```@Prefixed``` annotation
```java
@Prefixed
int childAge;
```
Then use
```java
userSettings.setChildAge("James", 13);
userSettings.setChildAge("Anna", 15);
```
###### Buffered edition
```java
userSettings.edit().setAge(18).setName("Stephen").apply();
```
*Note*
Until you not called ```edit()``` all values will be saved immediatly.
When you call ```edit()``` all next settings will be saved after calling ```apply()``` or ```commit()```

Example:
```java
userParams.edit().setAge(32).setName("Bob").apply();
```
###### Custom keywords
Mark the field with ```@Keyword``` for custom SharedPreferences key. For example it might help you to migrate to the library from manual preferences setting.
```java
@Keyword("NAME");
String name;
````
#### Supported types
Already supported only primitive types, its boxings and String

#### Limit the range of allowed values

For the limiting possible values for field, use `@Belongs` annotation.

Example:
```java
@Belongs("animals", "plants", "fungi", "chromista", "protist")
String eukaryoteKingdom

@Belongs("2", "3", "5", "7")
int smallPrimeNumber

@Belongs("0", "999999999999")
long npePerProject

@Belongs("-1", "0.43", "54.444f")
float randomNumber
```

**Note that** following examples will not compile:
```java
@Belongs("100", "50.5", "I don't know", "false", "90000000000")
int playerHp
//since floats, strings, booleans and longs are not allowed for ints

@Belongs("100", "50.5", "I don't know", "false", "90000000000")
long playerHp
//since floats, strings, booleans are not allowed for longs

@Belongs("100", "50.5", "I don't know", "false", "90000000000")
boolean whyIParticipatingInThis
//since only "true" and "false" allowed for booleans

@Belongs("100", "50.5", "I don't know", "false", "90000000000")
float playerHp
//since strings, booleans are not allowed for floats
```

#### Advanced
The library covers another important task you might need: set up some settings to the test builds without rebuilding. Usually programmers includes a special screen with the list of settings, and a tester should do some tricky actions to open it. The library let you take your settings out and manage them using special application provided with it.

1) Mark the class or aspecial fields with ```@Exportable```
2) Extend the abstract class ```PreferencesContentProvider```
```java
public class UserSettingsProvider extends PreferencesContentProvider {
    @Override
    protected Exporter getExporter() {
        return new PrefUserSettings(getContext().getSharedPreferences("main", Context.MODE_PRIVATE));
    }
}
```
3) Register it in your manifest
```xml
<provider
            android:name=".UserSettingsProvider"
            android:authorities="org.xelevra.prefdata.com.example.test"
            android:exported="true"/>
```
*Important*
In authorities you must write exactly the line started with "org.xelevra.prefdata." and end with your package name. Otherwice the browser app won't find your provider.
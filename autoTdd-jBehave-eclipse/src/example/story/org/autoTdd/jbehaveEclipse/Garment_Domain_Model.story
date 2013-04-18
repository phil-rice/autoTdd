Narrative:
In order to describe Garments
As a domain model engineer
I want to ensure that the GarmentType dominan model is unit tested

Scenario:  I can constuct garments using a string representing color and item
Given a garment called black sweater
Then I should have a garment type with the colour black and the name sweater

Scenario:  I gnore plurals on the item name
Given a garment called black sweaters
Then I should have a garment type with the colour black and the name sweater


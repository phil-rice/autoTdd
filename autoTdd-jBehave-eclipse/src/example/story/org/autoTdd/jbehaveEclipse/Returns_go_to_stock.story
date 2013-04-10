Narrative:
In order to keep track of stock
As a store owner
I want to add items back to stock when they're returned


Scenario:  Refunded items should be returned to stock
Given a customer previously bought 1 of black sweater from me
And I currently have 3 of black sweaters left in stock
When he returns the sweater for a refund
Then I should have 4 of black sweaters in stock
//so this then is an assertion
//other thens are conclusions... behave is different
// 

Scenario: Replaced items should be returned to stock

Given that a customer buys 1 of blue garment
And I have 2 of blue garments in stock
And I have 3 of black garments in stock
When he returns the garment for a replacement in black,
Then I should have 3 blue garments in stock
And I should have 2 black garments in stock
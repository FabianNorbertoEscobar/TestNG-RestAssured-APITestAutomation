import io.restassured.response.Response
import org.testng.Assert
import org.testng.annotations.Test

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath
import static io.restassured.RestAssured.*

class Books extends Base {

    @Test(groups="smoke")
    void getBooksList() {
        Response response = get("/books")
        ArrayList<String> allBooks = response.path("data.title")
        Assert.assertTrue(allBooks.size() > 1, "No books returned")
    }

    @Test(groups="regression")
    void booksSchemaIsValid() {
        get("/books")
            .then()
            .assertThat()
            .body(matchesJsonSchemaInClasspath("booksSchema.json"))
    }

    @Test(groups="smoke")
    void createAndDeleteBook() {
        File bookFile = new File(getClass().getResource("/book.json").toURI())
        Response createResponse = 
            given()
                .body(bookFile)
            .when()
                .post("/books")
        
        String responseID = createResponse.jsonPath().getString("post.book_id")
    
        Assert.assertEquals(201, createResponse.getStatusCode())

        Response deleteResponse =
            given()
                .body("{\n" + 
                    "\t\"book_id\": " + responseID + "\n" +
                    "}")
            .when()
                .delete("/books")
        
        Assert.assertEquals(200, deleteResponse.getStatusCode())
        Assert.assertEquals(deleteResponse.jsonPath().getString("message"), "Book successfully deleted")
    }

    @Test(groups="smoke")
    void deleteNonExistentBook_FailMessage() {
        String nonExistentBookID = "456123"
        
        Response deleteResponse =
            given()
                .body("{\n" + 
                    "\t\"book_id\": " + nonExistentBookID + "\n" +
                    "}")
            .when()
                .delete("/books")
        
        Assert.assertEquals(500, deleteResponse.getStatusCode())
        Assert.assertEquals(deleteResponse.jsonPath().getString("error"), "Unable to find book id: " + nonExistentBookID)
    }


}

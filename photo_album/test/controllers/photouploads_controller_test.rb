require 'test_helper'

class PhotouploadsControllerTest < ActionDispatch::IntegrationTest
  test "should get index" do
    get photouploads_index_url
    assert_response :success
  end

  test "should get create" do
    get photouploads_create_url
    assert_response :success
  end

end

require 'test_helper'

class SuggestionHistoriesControllerTest < ActionController::TestCase
  test "should get index" do
    get :index
    assert_response :success
    assert_not_nil assigns(:suggestion_histories)
  end

  test "should get new" do
    get :new
    assert_response :success
  end

  test "should create suggestion_history" do
    assert_difference('SuggestionHistory.count') do
      post :create, :suggestion_history => { }
    end

    assert_redirected_to suggestion_history_path(assigns(:suggestion_history))
  end

  test "should show suggestion_history" do
    get :show, :id => suggestion_histories(:one).to_param
    assert_response :success
  end

  test "should get edit" do
    get :edit, :id => suggestion_histories(:one).to_param
    assert_response :success
  end

  test "should update suggestion_history" do
    put :update, :id => suggestion_histories(:one).to_param, :suggestion_history => { }
    assert_redirected_to suggestion_history_path(assigns(:suggestion_history))
  end

  test "should destroy suggestion_history" do
    assert_difference('SuggestionHistory.count', -1) do
      delete :destroy, :id => suggestion_histories(:one).to_param
    end

    assert_redirected_to suggestion_histories_path
  end
end

require 'test_helper'

class StudySessionsControllerTest < ActionController::TestCase
  test "should get index" do
    get :index
    assert_response :success
    assert_not_nil assigns(:study_sessions)
  end

  test "should get new" do
    get :new
    assert_response :success
  end

  test "should create study_session" do
    assert_difference('StudySession.count') do
      post :create, :study_session => { }
    end

    assert_redirected_to study_session_path(assigns(:study_session))
  end

  test "should show study_session" do
    get :show, :id => study_sessions(:one).to_param
    assert_response :success
  end

  test "should get edit" do
    get :edit, :id => study_sessions(:one).to_param
    assert_response :success
  end

  test "should update study_session" do
    put :update, :id => study_sessions(:one).to_param, :study_session => { }
    assert_redirected_to study_session_path(assigns(:study_session))
  end

  test "should destroy study_session" do
    assert_difference('StudySession.count', -1) do
      delete :destroy, :id => study_sessions(:one).to_param
    end

    assert_redirected_to study_sessions_path
  end
end

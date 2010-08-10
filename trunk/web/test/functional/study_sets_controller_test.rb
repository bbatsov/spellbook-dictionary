require 'test_helper'

class StudySetsControllerTest < ActionController::TestCase
  test "should get index" do
    get :index
    assert_response :success
    assert_not_nil assigns(:study_sets)
  end

  test "should get new" do
    get :new
    assert_response :success
  end

  test "should create study_set" do
    assert_difference('StudySet.count') do
      post :create, :study_set => { }
    end

    assert_redirected_to study_set_path(assigns(:study_set))
  end

  test "should show study_set" do
    get :show, :id => study_sets(:one).to_param
    assert_response :success
  end

  test "should get edit" do
    get :edit, :id => study_sets(:one).to_param
    assert_response :success
  end

  test "should update study_set" do
    put :update, :id => study_sets(:one).to_param, :study_set => { }
    assert_redirected_to study_set_path(assigns(:study_set))
  end

  test "should destroy study_set" do
    assert_difference('StudySet.count', -1) do
      delete :destroy, :id => study_sets(:one).to_param
    end

    assert_redirected_to study_sets_path
  end
end

class PhotouploadsController < ApplicationController
  def index
    @img_paths=[]
    jpgs=Dir.glob(Rails.root.join('public','*jpg'))

    jpgs.each do |jpg|
      @img_paths.push('/'+File.basename(jpg))
    end

    @img_paths.shuffle!
  end


  def create
    uploaded_photo=photoupload_param[:file]
    output_path=Rails.root.join('public',uploaded_photo.original_filename)

    File.open(output_path,'w+b')do|fp|
      fp.write uploaded_photo.read
    end

    redirect_to action: 'index'
  end

  private
  def photoupload_param
    params.require(:photoupload).permit(:file)
  end
end

